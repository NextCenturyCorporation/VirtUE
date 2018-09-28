/**
 * @class
 * This class represents a breadcrumb, hold a link to a single page. Used to build a chain of links,
 * showing how the user got from a root page to the page they're on, and allowing them to safely return to any of those
 * previous pages.
 */
export class Breadcrumb {

  /**
   * just initializes those two fields as attributes
   */
  constructor(
    /** The text string which should show up on the screen as a clickable link */
    public label: string,
    /** the url to be navigated to, if the user clicks the breadcrumb */
    public href: string) {
  }
}
