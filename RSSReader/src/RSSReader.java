import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.xmltree.XMLTree;
import components.xmltree.XMLTree1;

/**
 * Program to convert an XML RSS (version 2.0) feed from a given URL into the
 * corresponding HTML output file.
 *
 * @author Vivian Lu
 *
 */
public final class RSSReader {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private RSSReader() {
    }

    /**
     * Outputs the "opening" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * <html> <head> <title>the channel tag title as the page title</title>
     * </head> <body>
     * <h1>the page title inside a link to the <channel> link</h1>
     * <p>
     * the channel description
     * </p>
     * <table border="1">
     * <tr>
     * <th>Date</th>
     * <th>Source</th>
     * <th>News</th>
     * </tr>
     *
     * @param channel
     *            the channel element XMLTree
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the root of channel is a <channel> tag] and out.is_open
     * @ensures out.content = #out.content * [the HTML "opening" tags]
     */
    private static void outputHeader(XMLTree channel, SimpleWriter out) {
        assert channel != null : "Violation of: channel is not null";
        assert out != null : "Violation of: out is not null";
        assert channel.isTag() && channel.label().equals("channel") : ""
                + "Violation of: the label root of channel is a <channel> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        String title = "";
        if (channel.child(getChildElement(channel, "title"))
                .numberOfChildren() == 0) {
            title = "Empty Title";
            out.println("<html> <head> <title>" + title + "</title>");
        } else {
            title = channel.child(getChildElement(channel, "title")).child(0)
                    .label();
            out.println("<html> <head> <title>" + title + "</title>");
        }

        out.println(" </head> <body>");

        if (channel.child(getChildElement(channel, "link"))
                .numberOfChildren() == 0) {
            out.println("<h1>No Link</h1>");
        } else {
            out.println(
                    "<h1><a href = "
                            + channel.child(getChildElement(channel, "link"))
                                    .child(0).label()
                            + ">" + title + "</a></h1>");
        }

        if (channel.child(getChildElement(channel, "description"))
                .numberOfChildren() == 0) {
            out.println("<p>No description </p>");
        } else {
            out.println("<p>"
                    + channel.child(getChildElement(channel, "description"))
                            .child(0).label()
                    + "</p>");
        }

        out.println("<table border=\"1\" >");
        out.println("<tr>");
        out.println("<th>Date</th>");
        out.println("<th>Source</th>");
        out.println("<th>News</th>");
        out.println("</tr>");

    }

    /**
     * Outputs the "closing" tags in the generated HTML file. These are the
     * expected elements generated by this method:
     *
     * </table>
     * </body> </html>
     *
     * @param out
     *            the output stream
     * @updates out.contents
     * @requires out.is_open
     * @ensures out.content = #out.content * [the HTML "closing" tags]
     */
    private static void outputFooter(SimpleWriter out) {
        assert out != null : "Violation of: out is not null";
        assert out.isOpen() : "Violation of: out.is_open";

        out.println("</table>");
        out.println("</body> </html>");

    }

    /**
     * Finds the first occurrence of the given tag among the children of the
     * given {@code XMLTree} and return its index; returns -1 if not found.
     *
     * @param xml
     *            the {@code XMLTree} to search
     * @param tag
     *            the tag to look for
     * @return the index of the first child of type tag of the {@code XMLTree}
     *         or -1 if not found
     * @requires [the label of the root of xml is a tag]
     * @ensures <pre>
     * getChildElement =
     *  [the index of the first child of type tag of the {@code XMLTree} or
     *   -1 if not found]
     * </pre>
     */
    private static int getChildElement(XMLTree xml, String tag) {
        assert xml != null : "Violation of: xml is not null";
        assert tag != null : "Violation of: tag is not null";
        assert xml.isTag() : "Violation of: the label root of xml is a tag";

        int val = -1;

        for (int i = 0; i < xml.numberOfChildren(); i++) {
            if (xml.child(i).label().equals(tag) && xml.child(i).isTag()) {
                val = i;
                i = xml.numberOfChildren();
            }
        }

        return val;
    }

    /**
     * Processes one news item and outputs one table row. The row contains three
     * elements: the publication date, the source, and the title (or
     * description) of the item.
     *
     * @param item
     *            the news item
     * @param out
     *            the output stream
     * @updates out.content
     * @requires [the label of the root of item is an <item> tag] and
     *           out.is_open
     * @ensures <pre>
     * out.content = #out.content *
     *   [an HTML table row with publication date, source, and title of news item]
     * </pre>
     */
    private static void processItem(XMLTree item, SimpleWriter out) {
        assert item != null : "Violation of: item is not null";
        assert out != null : "Violation of: out is not null";
        assert item.isTag() && item.label().equals("item") : ""
                + "Violation of: the label root of item is an <item> tag";
        assert out.isOpen() : "Violation of: out.is_open";

        /** the table row */
        out.println("<tr>");

        /**
         * output publication date if it exists
         */
        int num1 = getChildElement(item, "pubDate");
        if (num1 == -1) {
            out.println("<td>No date available</td>");
        } else {
            out.println("<td>" + item.child(num1).child(0).label() + "</td>");
        }

        /**
         * output source if it exists
         */
        int num2 = getChildElement(item, "source");
        if (num2 == -1) {
            out.println("<td>No source available</td>");
        } else {
            if (item.child(num2).numberOfChildren() == 0) {
                out.println("<td> <a href = "
                        + item.child(num2).attributeValue("url") + ">"
                        + "</a></td>");
            } else {
                out.println("<td> <a href = "
                        + item.child(num2).attributeValue("url") + ">"
                        + item.child(num2).child(0).label() + "</a></td>");
            }
        }

        /**
         * output title if it exists
         */
        int num3 = getChildElement(item, "title");
        if (num3 == -1) {
            /**
             * and then should check if it contains description:
             */
            int num4 = getChildElement(item, "description");
            if (num4 == -1) {
                out.println("<td>No description available</td>");
            } else {
                /**
                 * test if description tag has text child:
                 */
                String description = "";
                if (item.child(num4).numberOfChildren() != 0) {
                    description = item.child(num4).child(0).label();
                    /**
                     * should also check if it has link
                     */
                    int num5 = getChildElement(item, "link");
                    if (num5 == -1) {
                        out.println("<td>" + description + "</td>");
                    } else {
                        out.println("<td><a href = "
                                + item.child(num5).child(0).label() + ">"
                                + description + "</a></td>");
                    }
                } else {
                    out.println("<td><description></description></td>");
                }

            }
            out.println("<td>No title available</td>");
        } else {
            /**
             * test if title tag has text child:
             */
            String title = "";
            if (item.child(num3).numberOfChildren() != 0) {
                title = item.child(num3).child(0).label();
                /**
                 * should also check if it has link
                 */
                int num6 = getChildElement(item, "link");
                if (num6 == -1) {
                    out.println("<td>" + title + "</td>");
                } else {
                    out.println(
                            "<td><a href = " + item.child(num6).child(0).label()
                                    + ">" + title + "</a></td>");
                }

            } else {
                out.println("<td><title></title></td>");
            }

        }

        out.println("</tr>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments; unused here
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();

        out.print("Please input the URL of an RSS 2.0 feed: ");
        String url = in.nextLine();

        XMLTree xml = new XMLTree1(url);
        /**
         * To check check that the label of the root of the XMLTree is an <rss>
         * tag and that it has a version attribute with value "2.0"
         */
        String rootLabel = xml.label();

        String version = null;
        if (xml.hasAttribute("version")) {
            version = xml.attributeValue("version");
        }
        while (!xml.hasAttribute("version")
                || (xml.hasAttribute("version") && !version.equals("2.0"))
                || !rootLabel.equals("rss")) {
            out.println("Not a valid RSS 2.0 feed");
            out.print("Please input the URL of an RSS 2.0 feed: ");

            /**
             * update its properties
             */
            url = in.nextLine();
            xml = new XMLTree1(url);

            rootLabel = xml.label();

            if (!xml.hasAttribute("version")) {
                version = xml.attributeValue("version");
            }

        }

        /*
         * Extract <channel> element.
         */

        XMLTree channel = xml.child(0);
        out.print(
                "Please input the name of an output file including the .html extension: ");
        String fileName = in.nextLine();
        /**
         * use SimpleWriter to build a file accordingly,print everything to this
         * file
         */
        SimpleWriter webpage = new SimpleWriter1L(fileName);

        outputHeader(channel, webpage);

        /**
         * print the table to the file use processItem to generate a table of
         * items
         *
         * for each item, output title (or description, if title is not
         * available) and link (if available)
         */

        for (int i = 0; i < channel.numberOfChildren(); i++) {

            if (channel.child(i).label().equals("item")) {
                processItem(channel.child(i), webpage);
            }

        }
        outputFooter(webpage);

        webpage.close();
        in.close();
        out.close();
    }

}