package eu.mihosoft.freerouting.gui;

import java.util.Locale;

/**
 * Andrey Belomutskiy
 * 6/28/2014
 */
public class StartupOptions {
    boolean single_design_option = false;
    boolean test_version_option = false;
    boolean session_file_option = false;
    boolean webstart_option = false;
    String design_input_filename = null;
    String design_output_filename = null;
    String design_input_directory_name = null;
    int pass_number_first = 1;
    int pass_number_last = Integer.MAX_VALUE;
    java.util.Locale current_locale = java.util.Locale.ENGLISH;

    private StartupOptions() {
    }

    public Locale getCurrentLocale() {
        return current_locale;
    }

    public static StartupOptions parse(String[] p_args) {
        StartupOptions result = new StartupOptions();
        result.process(p_args);
        return result;
    }

    private void process(String[] p_args) {
        for (int i = 0; i < p_args.length; ++i) {

            if (p_args[i].startsWith("-de"))
            {
                // the design file is provided
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-")) {
                    single_design_option = true;
                    design_input_filename = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-di")) {
                // the design directory is provided
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-")) {
                    design_input_directory_name = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-do"))
            {
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-")) {
                    design_output_filename = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-pf"))
            {
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-")) {
                    pass_number_first = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-pl")) {
                // the design directory is provided
                if (p_args.length > i + 1 && !p_args[i + 1].startsWith("-")) {
                    pass_number_last = p_args[i + 1];
                }
            }
            else if (p_args[i].startsWith("-l"))
            {
                // the locale is provided
                if (p_args.length > i + 1 && p_args[i + 1].startsWith("d")) {
                    current_locale = java.util.Locale.GERMAN;
                }
            } else if (p_args[i].startsWith("-s")) {
                session_file_option = true;
            } else if (p_args[i].startsWith("-w")) {
                webstart_option = true;
            } else if (p_args[i].startsWith("-test")) {
                test_version_option = true;
            }
        }
    }

    public boolean getWebstartOption() {
        return webstart_option;
    }

    public boolean isTestVersion() {
        return test_version_option;
    }

    public String getDesignDir() {
        return design_input_directory_name;
    }
}
