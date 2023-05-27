package org.elyograg.test.haproxy;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Help;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.IHelpFactory;
import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;

// TODO: Auto-generated Javadoc
/**
 *
 * A class that holds configuration info and constants. Everything that is not
 * actually needed by the rest of the program is intentionally set to private,
 * beginning with the configuration file path.
 */
public final class StaticStuff {
  // Internal private stuff.

  /** The Constant log. */
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * The Constant USAGE_OPTION_SEPARATOR_TEXT.
   */
  public static final String USAGE_OPTION_SEPARATOR_TEXT = "\n"
      + "The usage above shows an equal sign for separating an option and its value."
      + " A space also works, and for single-character options, the separator is optional."
      + " Use quotes to wrap values that contain spaces or other special characters.";
  public static final List<Double> requestTimes = Collections.synchronizedList(new ArrayList<>());
  public static final AtomicInteger requestCounter = new AtomicInteger();

  private static AtomicInteger animCount = new AtomicInteger();
  private static AtomicLong animCheck = new AtomicLong();
  private static String lastLine = "";

  public static void sleep(final long duration, final TimeUnit unit) {
    long millis = 0L;
    int nanos = 0;

    switch (unit) {
    case SECONDS:
      nanos = 0;
      millis = duration * 1000;
      break;
    case MILLISECONDS:
      nanos = 0;
      millis = duration;
      break;
    case MINUTES:
      nanos = 0;
      millis = duration * 60000;
      break;
    case MICROSECONDS:
      millis = 0;
      nanos = (int) (duration * 1000);
    case NANOSECONDS:
      millis = 0;
      nanos = (int) duration;
    default:
      throw new RuntimeException(
          String.format("Unit %s not valid for this implementation.", unit.toString()));
    }
    try {
      Thread.sleep(millis, nanos);
    } catch (final InterruptedException e) {
      log.warn("Sleep of {} {} interrupted!", duration, unit.toString(), e);
    }
  }

  /**
   * This method was obtained from the picocli project issue tracker. It causes
   * options in the automatically generated help/usage text to all be
   * left-aligned. Without this, it indents some options further than other
   * options, which looks weird.
   *
   * @return {@link IHelpFactory} object for usage formatting.
   */
  public static final IHelpFactory createLeftAlignedUsageHelp() {
    return new IHelpFactory() {
      private static final int COLUMN_REQUIRED_OPTION_MARKER_WIDTH = 2;
      private static final int COLUMN_SHORT_OPTION_NAME_WIDTH = 2;
      private static final int COLUMN_OPTION_NAME_SEPARATOR_WIDTH = 2;
      private static final int COLUMN_LONG_OPTION_NAME_WIDTH = 22;

      private static final int INDEX_REQUIRED_OPTION_MARKER = 0;
      private static final int INDEX_SHORT_OPTION_NAME = 1;
      private static final int INDEX_OPTION_NAME_SEPARATOR = 2;
      private static final int INDEX_LONG_OPTION_NAME = 3;
      private static final int INDEX_OPTION_DESCRIPTION = 4;

      @Override
      public Help create(final CommandSpec commandSpec, final ColorScheme colorScheme) {
        return new Help(commandSpec, colorScheme) {
          @Override
          public Layout createDefaultLayout() {

            // The default layout creates a TextTable with 5 columns, as follows:
            // 0: empty text or (if configured) the requiredOptionMarker character
            // 1: short option name
            // 2: comma separator (if option has both short and long option)
            // 3: long option name(s)
            // 4: option description
            //
            // The code below creates a TextTable with 3 columns, as follows:
            // 0: empty text or (if configured) the requiredOptionMarker character
            // 1: all option names, comma-separated if necessary
            // 2: option description

            final int optionNamesColumnWidth = COLUMN_SHORT_OPTION_NAME_WIDTH
                + COLUMN_OPTION_NAME_SEPARATOR_WIDTH + COLUMN_LONG_OPTION_NAME_WIDTH;

            final TextTable table = TextTable.forColumnWidths(colorScheme,
                COLUMN_REQUIRED_OPTION_MARKER_WIDTH, optionNamesColumnWidth,
                commandSpec.usageMessage().width()
                    - (optionNamesColumnWidth + COLUMN_REQUIRED_OPTION_MARKER_WIDTH));
            final Layout result = new Layout(colorScheme, table, createDefaultOptionRenderer(),
                createDefaultParameterRenderer()) {
              @Override
              public void layout(final ArgSpec argSpec, final Ansi.Text[][] cellValues) {

                // The default option renderer produces 5 Text values for each option.
                // Below we combine the short option name, comma separator and long option name
                // into a single Text object, and we pass 3 Text values to the TextTable.
                for (final Ansi.Text[] original : cellValues) {
                  if (original[INDEX_OPTION_NAME_SEPARATOR].getCJKAdjustedLength() > 0) {
                    original[INDEX_OPTION_NAME_SEPARATOR] = original[INDEX_OPTION_NAME_SEPARATOR]
                        .concat(" ");
                  }
                  final Ansi.Text[] threeColumns = new Ansi.Text[] {
                      original[INDEX_REQUIRED_OPTION_MARKER],
                      original[INDEX_SHORT_OPTION_NAME]
                          .concat(original[INDEX_OPTION_NAME_SEPARATOR])
                          .concat(original[INDEX_LONG_OPTION_NAME]),
                      original[INDEX_OPTION_DESCRIPTION], };
                  table.addRowValues(threeColumns);
                }
              }
            };
            return result;
          }
        };
      }
    };
  }

  private static void print(final String line) {
    // clear the last line if longer
    if (lastLine.length() > line.length()) {
      String temp = "";
      for (int i = 0; i < lastLine.length(); i++) {
        temp += " ";
      }
      if (temp.length() > 1) {
        System.err.print("\r" + temp);
      }
    }
    System.err.print("\r" + line);
    lastLine = line;
  }

  public static void animate(final String line, final int skip) {
    if (animCheck.get() % skip == 0) {
      switch (animCount.get()) {
      case 1:
        print("[ \\ ] " + line);
        break;
      case 2:
        print("[ | ] " + line);
        break;
      case 3:
        print("[ / ] " + line);
        break;
      default:
        animCount.set(0);
        print("[ - ] " + line);
      }
      animCount.incrementAndGet();
    }
    animCheck.incrementAndGet();
    if (animCheck.get() == Long.MAX_VALUE) {
      animCheck.set(0);
    }
  }

  public static void exitProgram(final int... exitCode) {
    int code = 0;
    if (exitCode != null && exitCode.length > 0) {
      code = exitCode[0];
    }
    log.info("Exiting program, code {}", code);
    System.exit(code);
  }
}
