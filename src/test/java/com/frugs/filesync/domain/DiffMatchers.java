package com.frugs.filesync.domain;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class DiffMatchers {
    public static Matcher<Diff> hasContent(final String content) {
        return new TypeSafeMatcher<Diff>() {
            @Override protected boolean matchesSafely(Diff diff) {
                return content.equals(diff.toString());
            }

            @Override public void describeTo(Description description) {
                description.appendText("a diff with the content").appendValue(content);
            }
        };
    }
}
