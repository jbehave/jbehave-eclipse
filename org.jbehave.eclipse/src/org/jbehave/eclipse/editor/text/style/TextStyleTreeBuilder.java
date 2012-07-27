package org.jbehave.eclipse.editor.text.style;

public class TextStyleTreeBuilder {

    public TextStyle createTree(String rootKey) {
        TextStyle root = new TextStyle(rootKey, null);
        root.newChild(TextStyle.COMMENT);
        root.newChild(TextStyle.ERROR);
        createNarrativeSubTree(root);
        createStepSubTree(root);
        createScenarioSubTree(root);
        createExampleTableSubTree(root);
        createMetaSubTree(root);
        return root;
    }

    private void createMetaSubTree(TextStyle root) {
        TextStyle metaDefault = root.newChild(TextStyle.META_DEFAULT);
        metaDefault.newChild(TextStyle.META_KEYWORD);
    }

    private void createExampleTableSubTree(TextStyle root) {
        TextStyle exampleTableDefault = root.newChild(TextStyle.EXAMPLE_TABLE_DEFAULT);
        exampleTableDefault.newChild(TextStyle.EXAMPLE_TABLE_KEYWORD);
        exampleTableDefault.newChild(TextStyle.EXAMPLE_TABLE_SEPARATOR);
        exampleTableDefault.newChild(TextStyle.EXAMPLE_TABLE_CELL);
    }

    private void createScenarioSubTree(TextStyle root) {
        TextStyle scenarioDefault = root.newChild(TextStyle.SCENARIO_DEFAULT);
        scenarioDefault.newChild(TextStyle.SCENARIO_KEYWORD);
    }

    private void createStepSubTree(TextStyle root) {
        TextStyle stepDefault = root.newChild(TextStyle.STEP_DEFAULT);
        stepDefault.newChild(TextStyle.STEP_KEYWORD);
        stepDefault.newChild(TextStyle.STEP_PARAMETER);
        stepDefault.newChild(TextStyle.STEP_PARAMETER_VALUE);
        stepDefault.newChild(TextStyle.STEP_EXAMPLE_TABLE_SEPARATOR);
        stepDefault.newChild(TextStyle.STEP_EXAMPLE_TABLE_CELL);
    }

    private void createNarrativeSubTree(TextStyle root) {
        TextStyle narrativeDefault = root.newChild(TextStyle.NARRATIVE_DEFAULT);
        narrativeDefault.newChild(TextStyle.NARRATIVE_KEYWORD);
    }
}
