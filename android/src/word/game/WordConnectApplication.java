package word.game;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.acra.*;
import org.acra.config.*;
import org.acra.data.StringFormat;

public class WordConnectApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Only enable ACRA for release builds (not debug)
        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0) {
            ACRA.init(this, new CoreConfigurationBuilder()
                .withBuildConfigClass(BuildConfig.class)
                .withReportFormat(StringFormat.JSON)
                .withPluginConfigurations(
                    new MailSenderConfigurationBuilder()
                        .withMailTo("singomediaco@gmail.com")
                        .withReportAsFile(true)
                        .withReportFileName("word-connect-crash.txt")
                        .build(),
                    new DialogConfigurationBuilder()
                        .withTitle("Word Connect Crashed")
                        .withText("Sorry, the app crashed. A crash report will be sent to the developer to help fix this issue.")
                        .withCommentPrompt("Optional: describe what you were doing:")
                        .withPositiveButtonText("Send Report")
                        .withNegativeButtonText("Cancel")
                        .build()
                )
            );
        }
    }
}