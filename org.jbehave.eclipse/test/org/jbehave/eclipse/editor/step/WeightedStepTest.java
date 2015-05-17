package org.jbehave.eclipse.editor.step;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class WeightedStepTest {

  @Test
  public void testSorting() {
    WeightedStep s1 = new WeightedStep(null, 1.1f);
    WeightedStep s1Also = new WeightedStep(null, 1.1f);
    WeightedStep s2 = new WeightedStep(null, 1.2f);
    WeightedStep s3 = new WeightedStep(null, 1.3f);

    List<WeightedStep> list = Lists.newArrayList(s1, s3, s2, s1Also);
    Collections.sort(list);

    // Note we expect the relative ordering of s1Also and s1 to be preserved as they're equal
    List<WeightedStep> expectedSortOrder = ImmutableList.of(s1, s1Also, s2, s3);
    assertEquals(expectedSortOrder, list);

    assertEquals(0, s1.compareTo(s1Also));
  }

}
