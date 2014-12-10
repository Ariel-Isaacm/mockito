package org.mockito.internal.configuration.plugins;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.io.IOUtil;
import org.mockito.plugins.PluginSwitcher;
import org.mockitoutil.TestBase;

import java.io.File;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class PluginFinderTest extends TestBase {

    @Mock PluginSwitcher switcher;
    @InjectMocks PluginFinder finder;
    public @Rule TemporaryFolder tmp = new TemporaryFolder();

    @Test public void empty_resources() {
        assertNull(finder.findPluginClass((Iterable) asList()));
    }

    @Test public void no_valid_impl() throws Exception {
        File f = tmp.newFile();

        //when
        IOUtil.writeText("  \n  ", f);

        //then
        assertNull(finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

    @Test public void single_implementation() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(true);

        //when
        IOUtil.writeText("  foo.Foo  ", f);

        //then
        assertEquals("foo.Foo", finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

    @Test public void single_implementation_disabled() throws Exception {
        File f = tmp.newFile();
        when(switcher.isEnabled("foo.Foo")).thenReturn(false);

        //when
        IOUtil.writeText("  foo.Foo  ", f);

        //then
        assertEquals(null, finder.findPluginClass((Iterable) asList(f.toURI().toURL())));
    }

    @Test public void multiple_implementations_only_one_enabled() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled("Bar")).thenReturn(true);

        //when
        IOUtil.writeText("Foo", f1); IOUtil.writeText("Bar", f2);

        //then
        assertEquals("Bar", finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test public void multiple_implementations_only_one_useful() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled(anyString())).thenReturn(true);

        //when
        IOUtil.writeText("   ", f1); IOUtil.writeText("X", f2);

        //then
        assertEquals("X", finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test public void multiple_empty_implementations() throws Exception {
        File f1 = tmp.newFile(); File f2 = tmp.newFile();

        when(switcher.isEnabled(anyString())).thenReturn(true);

        //when
        IOUtil.writeText("   ", f1); IOUtil.writeText("\n", f2);

        //then
        assertEquals(null, finder.findPluginClass((Iterable) asList(f1.toURI().toURL(), f2.toURI().toURL())));
    }

    @Test public void problems_loading_impl() throws Exception {
        when(switcher.isEnabled(anyString())).thenThrow(new RuntimeException("Boo!"));

        try {
            //when
            finder.findPluginClass((Iterable) asList(new File("xxx").toURI().toURL()));
            //then
            fail();
        } catch(Exception e) {
            assertContains("xxx", e.getMessage());
            e.getCause().getMessage().equals("Boo!");
        }
    }
}