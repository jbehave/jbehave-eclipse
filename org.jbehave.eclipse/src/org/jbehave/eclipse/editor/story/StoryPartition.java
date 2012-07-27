package org.jbehave.eclipse.editor.story;

import java.util.List;

import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.util.New;

public enum StoryPartition {

    Step,
    Narrative,
    ExampleTable,
    Comment,
    Scenario,
    Meta,
    Undefined;
    
    public static boolean arePartitionsEqual(Keyword keyword1, Keyword keyword2) {
        return partitionOf(keyword1)==partitionOf(keyword2);
    }
    
    public static List<String> names () {
        List<String> types = New.arrayList();
        for(StoryPartition partition : StoryPartition.values())
            types.add(partition.name());
        return types;
    }
    
    public static StoryPartition partitionOf(Keyword keyword) {
        if(keyword==null)
            return Undefined;
        switch(keyword) {
            case Given:
            case When:
            case Then:
            case And : 
                return Step;
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableValueSeparator:
            case ExamplesTableRow:
                return ExampleTable;
            case Ignorable:
                return Comment;
            case Narrative:
            case AsA:
            case InOrderTo:
            case IWantTo:
                return Narrative;
            case Scenario:
                return Scenario;
            case GivenStories: 
            case Meta: 
            case MetaProperty:
                return Meta;
        }
        return Undefined;
    }
}
