package org.jbehave.eclipse;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.eclipse.Keyword.And;
import static org.jbehave.eclipse.Keyword.AsA;
import static org.jbehave.eclipse.Keyword.ExamplesTable;
import static org.jbehave.eclipse.Keyword.ExamplesTableHeaderSeparator;
import static org.jbehave.eclipse.Keyword.ExamplesTableIgnorableSeparator;
import static org.jbehave.eclipse.Keyword.ExamplesTableRow;
import static org.jbehave.eclipse.Keyword.ExamplesTableValueSeparator;
import static org.jbehave.eclipse.Keyword.Given;
import static org.jbehave.eclipse.Keyword.GivenStories;
import static org.jbehave.eclipse.Keyword.IWantTo;
import static org.jbehave.eclipse.Keyword.Ignorable;
import static org.jbehave.eclipse.Keyword.InOrderTo;
import static org.jbehave.eclipse.Keyword.Meta;
import static org.jbehave.eclipse.Keyword.MetaProperty;
import static org.jbehave.eclipse.Keyword.Narrative;
import static org.jbehave.eclipse.Keyword.Scenario;
import static org.jbehave.eclipse.Keyword.Then;
import static org.jbehave.eclipse.Keyword.When;

import java.util.Locale;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.junit.Test;

public class KeywordTest {

    @Test
    public void asString() {
        Keywords keywords = new LocalizedKeywords(Locale.US);
        assertThat(Meta.asString(keywords), equalTo("Meta:"));
        assertThat(MetaProperty.asString(keywords), equalTo("@"));
        assertThat(Narrative.asString(keywords), equalTo("Narrative:"));
        assertThat(InOrderTo.asString(keywords), equalTo("In order to"));
        assertThat(AsA.asString(keywords), equalTo("As a"));
        assertThat(IWantTo.asString(keywords), equalTo("I want to"));
        assertThat(Scenario.asString(keywords), equalTo("Scenario:"));
        assertThat(GivenStories.asString(keywords), equalTo("GivenStories:"));
        assertThat(ExamplesTable.asString(keywords), equalTo("Examples:"));
        assertThat(ExamplesTableRow.asString(keywords), equalTo("Example:"));
        assertThat(ExamplesTableHeaderSeparator.asString(keywords), equalTo("|"));
        assertThat(ExamplesTableValueSeparator.asString(keywords), equalTo("|"));
        assertThat(ExamplesTableIgnorableSeparator.asString(keywords), equalTo("|--"));
        assertThat(Given.asString(keywords), equalTo("Given"));
        assertThat(When.asString(keywords), equalTo("When"));
        assertThat(Then.asString(keywords), equalTo("Then"));
        assertThat(And.asString(keywords), equalTo("And"));
        assertThat(Ignorable.asString(keywords), equalTo("!--"));
    }
}
