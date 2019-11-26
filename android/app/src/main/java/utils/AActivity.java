package utils;

import android.app.Activity;
import android.os.Bundle;

/** Base class for a console-based activity that will run Python code. sys.stdout and sys.stderr
 * will be directed to the output view whenever the activity is resumed. If the Python code
 * caches their values, it can direct output to the activity even when it's paused.
 *
 * If STDIN_ENABLED is passed to the Task constructor, sys.stdin will also be redirected whenever
 * the activity is resumed. The input box will initially be hidden, and will be displayed the
 * first time sys.stdin is read. */
public  class AActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        //py.getModule("electrum_gui.android.daemon").callAttr("test");
    }

}
