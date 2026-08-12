# IntelliJ IDEA code style preferences

This project uses [Spotless](https://github.com/diffplug/spotless) for consistent formatting of the project's code and integrates the corresponding Gradle plugin, which can be used to apply the configured formatting rules.
Although this is already sufficient to enable development in this project, executing a Gradle task to format (new) code does not necessarily provide a fluent development experience (as this can take a few seconds).
To improve this situation when working with IntelliJ IDEA, IDE-specific configuration files can be found in the directory this file is located.
With these files, IDE-specific formatting is configured to be as close as possible to formatting with Spotless.

## Updating IntelliJ IDEA code style preferences

The formatting rules that Spotless applies are defined in `config/eclipse-formatter-preferences.xml`.
If the content of this file changes, the IDE-specific configuration files probably have to be updated as well.
To do this, go to `Settings -> Editor -> Code Style -> Java`, click on the settings icon next to the scheme selection and select `Import Scheme -> Eclipse XML Profile`.
Select the `eclipse-formatter-preferences.xml` file and be sure to select `To: Current Scheme` in the "Import Scheme" dialog.
In order for the updated configuration to be persisted in the configuration files, be sure to manually adjust one (any) setting, click "Apply", reset it to the original value and click "Apply" again.

## Manual adjustments to the IntelliJ IDEA code style preferences

Unfortunately, despite the import described above, a complete match between the formatting applied by Spotless and the IDE-specific formatting cannot be achieved.
The following code style preferences may have to be adjusted manually after an import:

* Wrapping and Braces -> Throws list -> Wrap if long
