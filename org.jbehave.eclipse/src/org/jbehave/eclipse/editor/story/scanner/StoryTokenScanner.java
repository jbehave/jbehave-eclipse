package org.jbehave.eclipse.editor.story.scanner;

import static org.jbehave.eclipse.util.Objects.o;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.ContentWithIgnorableEmitter;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.parser.StoryVisitor;
import org.jbehave.eclipse.parser.RegexUtils.TokenizerCallback;
import org.jbehave.eclipse.parser.ContentWithIgnorableEmitter.Callback;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * A token scanner scans a range of a document and reports about the token it finds. 
 * <b>A scanner has state</b>. When asked, the scanner returns the offset and the length 
 * of the last found token.
 * </p>
 */
public abstract class StoryTokenScanner implements ITokenScanner {
    
    private Logger log = LoggerFactory.getLogger(StoryTokenScanner.class);
    
    protected final TextAttributeProvider textAttributeProvider;
    protected final JBehaveProject jbehaveProject;
    //
    private IToken defaultToken;
    private Token commentToken;
    protected Token exampleTableSepToken;
    protected Token exampleTableCellToken;
    //
    private List<Fragment> fragments;
    private int cursor = 0;
    //
    protected IDocument document;
    protected Region range;

    public StoryTokenScanner(JBehaveProject jbehaveProject, TextAttributeProvider textAttributeProvider) {
        this.jbehaveProject = jbehaveProject;
        this.textAttributeProvider = textAttributeProvider;
        textAttributeProvider.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                initialize();
            }
        });

    }
    
    /**
     * Initialize the scanner. This method is also called whenever {@link TextAttribute} get modified.
     */
    protected void initialize() {
        commentToken = newToken(TextStyle.COMMENT);
    }
    
    /**
     * Create a new token whose data is the {@link TextAttribute} matching the given styleId.
     * @param styleId
     * @return
     */
    protected Token newToken(String styleId) {
        TextAttribute textAttribute = textAttributeProvider.get(styleId);
        return new Token(textAttribute);
    }
    
    public void setDefaultToken(IToken defaultToken) {
        if(defaultToken==null)
            throw new IllegalArgumentException();
        this.defaultToken = defaultToken;
    }
    
    public IToken getDefaultToken() {
        return defaultToken;
    }
    
    @Override
    public int getTokenLength() {
        return fragments.get(cursor).getLength();
    }
    
    @Override
    public int getTokenOffset() {
        return fragments.get(cursor).getOffset();
    }
    
    @Override
    public IToken nextToken() {
        if(cursor==-1) {
            evaluateFragments();
        }
        cursor++;
        if(cursor<fragments.size())
            return fragments.get(cursor).getToken();
        return Token.EOF;
    }
    
    protected void evaluateFragments() {
        final Object self = this;
        StoryVisitor visitor = new StoryVisitor() {
            @Override
            public void visit(StoryElement element) {
                if(element.intersects(range.getOffset(), range.getLength())) {
                    if(isAccepted(element)){
                        emit(element); // element is given an absolute position
                    } else {
                        log.warn("Element rejected... ({}@{}): {}", Objects.o(self.getClass(), System.identityHashCode(self), element));
                    }
                }
            }
        };
        new StoryDocumentUtils(getLocalizedStepSupport()).traverseStory(document, visitor);
        
        consolidateFragments();
    }

    private void consolidateFragments() {
        log.debug("Consolidating fragments ({}@{})", getClass(), System.identityHashCode(this));
        int start = range.getOffset();
        int length = range.getLength();

        try {
            if(defaultToken==null)
                throw new IllegalStateException("No default token defined");
            
            if(fragments.isEmpty()) {
                emitCommentAware(defaultToken, start, document.get(start, length));
                return;
            }
            Fragment firstFragment = fragments.get(0);
            if(firstFragment.offset>start) {
                fragments.add(0, new Fragment(defaultToken, start, firstFragment.offset-start));
            }
            
            Fragment lastFragment = fragments.get(fragments.size()-1);
            int endOffset = start + length;
            int endFragmentOffset = lastFragment.offset+lastFragment.length;
            if(endFragmentOffset<endOffset) {
                log.debug("Emitting consolidation fragment at offset: {} of length: {} (document length: {})", o(endFragmentOffset, endOffset - endFragmentOffset, document.getLength()));
                emitCommentAware(defaultToken, endFragmentOffset, 
                        document.get(endFragmentOffset, endOffset - endFragmentOffset));
            }
        } catch (BadLocationException e) {
            log.error("Failed to consolidate fragments", e);
        }
        
        int expected = 0;
        for(Fragment fragment : fragments) {
            if(fragment.offset!=expected)
                log.warn("humpff");
            log.debug("fragment: {}, {}, {}", o(fragment.offset, fragment.length, fragment.token.getData()));
            expected = fragment.offset + fragment.length;
        }
    }

    protected LocalizedStepSupport getLocalizedStepSupport() {
        return jbehaveProject.getLocalizedStepSupport();
    }
    
    protected abstract boolean isAccepted(StoryElement element);

    protected static String f(String string) {
        return string.replace("\n", "\\n");
    }
    
    protected abstract void emit(StoryElement element);
    
    protected void emit(ContentWithIgnorableEmitter emitter, IToken token, int offset, int length) {
        emitter.emitNext(offset, length, emitterCallback(), token);
    }
    
    private Callback<IToken> emitterCallback;
    private Callback<IToken> emitterCallback() {
        if(emitterCallback==null) {
            emitterCallback = new Callback<IToken>() {
                @Override
                public void emit(IToken arg, int offset, int length) {
                    StoryTokenScanner.this.emit(arg, offset, length);
                }
                @Override
                public void emitIgnorable(int offset, int length) {
                    emit(commentToken, offset, length);
                }
            };
        }
        return emitterCallback;
    }

    protected void emit(IToken token, int offset, int length) {
        log.debug("Emitting ({}, offset: {}, length: {})",
                  o(token.getData(), offset, length));
        if(length==0) {
            log.debug("Empty token emitted zero length, data: {},  offset: {}, length: {}", o(token.getData(), offset, length));
        }
        else if(length<0) {
            log.error("Invalid token emitted negative length, data: {},  offset: {}, length: {}", o(token.getData(), offset, length));
        }
        else {
            log.debug("Token emitted, data: {},  offset: {}, length: {}, content: <{}>", o(token.getData(), offset, length, getContentForLog(offset, length)));
        }
        
        // can we merge previous one?
        if(!fragments.isEmpty()) {
            Fragment previous = getLastFragment();
            
            // check no hole
            int requiredOffset = previous.offset+previous.length;
            if(offset != requiredOffset) {
                log.debug("**hole completion**, offset: {} (vs required: {}), length: {}; previous offset: {}, length: {}",
                        o(offset, requiredOffset, length, previous.offset, previous.length));
                emit(getDefaultToken(), requiredOffset, offset-requiredOffset);
                previous = getLastFragment();
            }
            
            if(previous.token==token) {
                previous.length += length;
                log.debug("Token merged, offset: {}, length: {}", o(previous.offset, previous.length));
                return;
            }
        }
        Fragment fragment = new Fragment(token, offset, length);
        addFragment(fragment);
    }
    
    public void addFragment(Fragment fragment) {
        log.debug("Fragment added, offset: {}, length: {}, token: {}", o(fragment.offset, fragment.length, fragment.token));
        fragments.add(fragment);
    }
    
    public void addFragments(Iterable<Fragment> fragments) {
        for(Fragment fragment : fragments)
            addFragment(fragment);
    }
    
    public List<Fragment> getFragments() {
        return fragments;
    }

    private String getContentForLog(int offset, int length) {
        return f(getContent(offset, length));
    }

    private String getContent(int offset, int length)  {
        try {
            return document.get(offset, length);
        } catch (BadLocationException e) {
            return "<<<n/a>>>";
        }
    }

    private Fragment getLastFragment() {
        return fragments.get(fragments.size()-1);
    }
    
    protected void emitTable(final ContentWithIgnorableEmitter emitter, final IToken defaultToken, final int offset, String content) {
        RegexUtils.splitLine(content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String line, boolean isDelimiter) {
                if(isDelimiter)
                    emit(emitter, defaultToken, offset + startOffset, line.length());
                else if(line.trim().startsWith("|--"))
                    emit(emitter, commentToken, offset + startOffset, line.length());
                else
                    emitTableRow(emitter, defaultToken, offset + startOffset, line);
            }
        });
    }
    
    protected void emitCommentAware(final IToken defaultToken, final int offset, String content) {
        RegexUtils.splitLine(content, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String line, boolean isDelimiter) {
                if(line.trim().startsWith("!--"))
                    emit(commentToken, offset + startOffset, line.length());
                else
                    emit(defaultToken, offset + startOffset, line.length());
            }
        });
    }

    public Chain commentAwareChain(final IToken token) {
        return new Chain() {
            @Override
            public void next(int offset, String content) {
                emitCommentAware(token, offset, content);
            }
        };
    }
    
    protected void emitTableRow(ContentWithIgnorableEmitter emitter, IToken defaultToken, int offset, String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, "|", true);
        int remaining = tokenizer.countTokens();
        boolean isFirst = true;
        while(tokenizer.hasMoreTokens()) {
            boolean isLast = (remaining==1);
            String tok = tokenizer.nextToken();
            int length = tok.length();
            
            if(tok.equals("|")) {
                emit(emitter, exampleTableSepToken, offset, length);
            }
            else if(isLast || isFirst) {
                emit(emitter, defaultToken, offset, length);
            }
            else {
                emit(emitter, exampleTableCellToken, offset, length);
            }
            
            offset += length;
            remaining--;
            isFirst = false;
        }
    }

    /**
     * Configures the scanner by providing access to the document range that should be scanned.
     * 
     * @see org.eclipse.jface.text.rules.ITokenScanner#setRange(org.eclipse.jface.text.IDocument, int, int)
     */
    @Override
    public void setRange(IDocument document, int offset, int length) {
        log.debug("Range(offset: " +  offset + ", length: " + length + ", document length: " + document.getLength() + ")");
        if((offset+length)>document.getLength())
            throw new IllegalArgumentException("Range is outside the document");

        fragments = New.arrayList();
        cursor = -1;
        this.document = document;
        this.range = new Region(offset, length);
    }
     
    public class Fragment {
        private IToken token;
        private int offset, length;
        private Fragment(IToken token, int offset, int length) {
            super();
            this.token = token;
            this.offset = offset;
            this.length = length;
        }
        @Override
        public String toString() {
            try {
                return token.getData() + ", offset: " + offset + ", length: " + length + ", c>>" + document.get(offset, length)+"<<";
            } catch (BadLocationException e) {
                return token.getData() + ", offset: " + offset + ", length: " + length + ", c>>" + "//BadLocationException//" +"<<";
            }
        }
        public int getOffset() {
            return offset;
        }
        public int getLength() {
            return length;
        }
        public IToken getToken() {
            return token;
        }
        public boolean intersects(Region range) {
            int tMin = offset;
            int tMax = offset+length-1;
            int oMin = range.getOffset();
            int oMax = range.getOffset()+range.getLength()-1;
            return tMin<=oMax && oMin<=tMax;
        }
    }
    
    public interface Chain {
        void next(int offset, String content);
    }

}
