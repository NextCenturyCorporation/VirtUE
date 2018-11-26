package com.ncc.savior.virtueadmin.model;

// import javax.persistence.Embeddable;

/**
 * adapted from https://stackoverflow.com/questions/3978654/best-way-to-create-enum-of-strings
 *
 */
// @Embeddable
public enum NetworkProtocol {
    TCPIP("TCP/IP"),
    UDPIP("UDP/IP"),
    ICMP("ICMP")
    ;

    private final String text;

    /**
		 *
     */
    NetworkProtocol(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }

    // @JsonGetter
    // getText() {
    //   return text;
    // }
    //
    // @JsonSetter
    // setText( String text ) {
    //   this.text = text;
    // }
}
