package org.jbehave.eclipse.preferences;

import java.util.Locale;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.PropertyPage;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.KeywordImages;
import org.jbehave.eclipse.util.LocaleUtils;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPreferencePage extends PropertyPage implements
		org.eclipse.ui.IWorkbenchPreferencePage {

	private Logger logger = LoggerFactory
			.getLogger(ProjectPreferencePage.class);
	//
	private Combo languageCombo;
	private Locale[] locales = { Locale.ENGLISH, Locale.ENGLISH };
	private Table table;
	private TableViewer localizedKeywords;
	private IProject project;
	private Button enableProjectSpecific;
	private ProjectPreferences prefs;
	private ControlEnableState blockEnableState;
	private Composite projectComposite;
	private Text parameterPrefixText;

	/**
	 * Create the preference page.
	 */
	public ProjectPreferencePage() {
	}

	/**
	 * Create contents of the preference page.
	 * 
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));

		if (isProjectPreferencePage()) {
			enableProjectSpecific = new Button(container, SWT.CHECK);
			enableProjectSpecific.setLayoutData(new GridData(SWT.LEFT,
					SWT.CENTER, false, false, 1, 1));
			enableProjectSpecific.setText("Enable project specific settings");
			enableProjectSpecific.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					adjustProjectSpecificState();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent event) {
					adjustProjectSpecificState();
				}
			});
		}

		projectComposite = new Composite(container, SWT.NONE);
		projectComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				true, 1, 1));
		projectComposite.setLayout(new GridLayout(2, false));

		Label lblParameterPrefix = new Label(projectComposite, SWT.NONE);
		lblParameterPrefix.setSize(87, 14);
		lblParameterPrefix.setText("Parameter prefix");

		parameterPrefixText = new Text(projectComposite, SWT.NONE | SWT.BORDER);
		parameterPrefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				false, false, 1, 1));
		parameterPrefixText.setSize(100, 22);

		Label lblStoryLanguage = new Label(projectComposite, SWT.NONE);
		lblStoryLanguage.setSize(87, 14);
		lblStoryLanguage.setText("Story Language");

		languageCombo = new Combo(projectComposite, SWT.READ_ONLY);
		languageCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false,
				false, 1, 1));
		languageCombo.setSize(279, 22);
		languageCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent event) {
				widgetSelected(event);
			}

			@Override
			public void widgetSelected(SelectionEvent event) {
				int selectionIndex = languageCombo.getSelectionIndex();
				setLanguage(languageCombo.getItem(selectionIndex));
			}
		});
		new Label(projectComposite, SWT.NONE);
		new Label(projectComposite, SWT.NONE);

		localizedKeywords = new TableViewer(projectComposite,
				SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL
						| SWT.FULL_SELECTION | SWT.BORDER);
		localizedKeywords.setColumnProperties(new String[] { "English",
				"Selected" });
		localizedKeywords
				.setContentProvider(ArrayContentProvider.getInstance());
		localizedKeywords.setLabelProvider(new KeywordTableLabelProvider(
				Activator.getDefault().getKeywordImages()));
		localizedKeywords.setInput(Keyword.values());
		table = localizedKeywords.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setSize(371, 289);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// English column serves as reference
		TableColumn columnEn = new TableColumn(table, SWT.LEFT);
		columnEn.setText("English");
		columnEn.setWidth(150);

		// selected locale column
		TableColumn columnSel = new TableColumn(table, SWT.LEFT);
		columnSel.setText("Selected");
		columnSel.setWidth(250);

		reload();
		updatePageWithPrefs();

		return container;
	}

	@Override
	public void setElement(final IAdaptable element) {
		project = (IProject) element.getAdapter(IResource.class);
		super.setElement(element);
	}

	private boolean isProjectPreferencePage() {
		return project != null;
	}

	private void setLanguage(String language) {
		this.locales[1] = LocaleUtils.createLocaleFromCode(language,
				Locale.ENGLISH);
		this.localizedKeywords.refresh(true);
	}

	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench) {
		// Initialize the preference page
	}

	protected void storePrefs() {
		try {
			updatePrefsWithPage();
			if (isProjectPreferencePage()
					&& !enableProjectSpecific.getSelection()) {
				prefs.removeAllSpecificSettings();
			}
			prefs.store();
		} catch (final BackingStoreException e) {
			Activator.logError("Failed to store preferences", e);
		}
	}

	@Override
	public boolean performOk() {
		storePrefs();
		reload();
		updatePageWithPrefs();
		return super.performOk();
	}

	@Override
	protected void performDefaults() {
		try {
			prefs.removeAllSpecificSettings();
		} catch (BackingStoreException e) {
			Activator.logError("Failed to remove specific settings", e);
		}
		reload();
		updatePageWithPrefs();
		super.performDefaults();
	}

	private void reload() {
		if (project == null) {
			prefs = new ProjectPreferences();
		} else {
			prefs = new ProjectPreferences(project);
		}
		try {
			prefs.load();
		} catch (BackingStoreException e) {
			Activator.logError("Failed to load preferences", e);
		}
	}

	private void updatePrefsWithPage() {
		if (isProjectPreferencePage()) {
			boolean isProjectSpecific = enableProjectSpecific.getSelection();
			prefs.setUseProjectSettings(isProjectSpecific);
		}
		prefs.setParameterPrefix(parameterPrefixText.getText());
		prefs.setStoryLanguage(locales[1].toString());

		logger.debug(
				"Updating prefs with story language <{}> and parameter prefix <{}>",
				prefs.getStoryLanguage(), prefs.getParameterPrefix());
	}

	private void updatePageWithPrefs() {
		parameterPrefixText.setText(prefs.getParameterPrefix());
		String[] langs = prefs.availableStoryLanguages();
		String selectedLanguage = prefs.getStoryLanguage();
		languageCombo.setItems(langs);
		languageCombo.select(ArrayUtils.indexOf(langs, selectedLanguage));
		setLanguage(selectedLanguage);
		if (isProjectPreferencePage()) {
			enableProjectSpecific.setSelection(prefs.isUseProjectSettings());
			adjustProjectSpecificState();
		}
	}

	private void adjustProjectSpecificState() {
		boolean useProjectSpecificSettings = enableProjectSpecific
				.getSelection();
		if (useProjectSpecificSettings) {
			if (blockEnableState != null) {
				blockEnableState.restore();
				blockEnableState = null;
			}
		} else {
			if (blockEnableState == null) {
				blockEnableState = ControlEnableState.disable(projectComposite);
			}
		}
	}

	private class KeywordTableLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		private KeywordImages keywordImages;

		public KeywordTableLabelProvider(KeywordImages keywordImages) {
			this.keywordImages = keywordImages;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return keywordImages.getImageFor((Keyword) element);
			}
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			return ((Keyword) element).asString(keywordsFor(columnIndex));
		}
	}

	private Keywords keywordsFor(int columnIndex) {
		return new LocalizedKeywords(locales[columnIndex]);
	}

}
