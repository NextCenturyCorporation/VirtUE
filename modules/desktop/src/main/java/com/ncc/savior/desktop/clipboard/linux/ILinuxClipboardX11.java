package com.ncc.savior.desktop.clipboard.linux;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.unix.X11;

/**
 * Interface to wrap the Xlib library and add functions that JNA doesn't
 * natively contain.
 *
 */
public interface ILinuxClipboardX11 extends X11 {
	ILinuxClipboardX11 INSTANCE = (ILinuxClipboardX11) Native.loadLibrary("X11", ILinuxClipboardX11.class);

	/**
	 * See https://tronche.com/gui/x/xlib/window-information/XGetSelectionOwner.html
	 *
	 * Arguments
	 *
	 * @param display
	 *            Specifies the connection to the X server.
	 * @param selection
	 *            Specifies the selection atom whose owner you want returned.
	 *
	 *
	 *            Description The XGetSelectionOwner() function returns the window
	 *            ID associated with the window that currently owns the specified
	 *            selection. If no selection was specified, the function returns the
	 *            constant None . If None is returned, there is no owner for the
	 *            selection. XGetSelectionOwner() can generate a BadAtom error.
	 *
	 *            Diagnostics BadAtom A value for an Atom argument does not name a
	 *            defined Atom.
	 *
	 */
	Window XGetSelectionOwner(Display display, Atom selection);

	/**
	 * Arguments
	 *
	 * @param display
	 *            Specifies the connection to the X server.
	 * @param selection
	 *            Specifies the selection atom.
	 * @param owner
	 *            Specifies the owner of the specified selection atom. You can pass
	 *            a window or None .
	 * @param time
	 *            Specifies the time. You can pass either a timestamp or
	 *            CurrentTime.
	 *
	 *            See
	 *            https://tronche.com/gui/x/xlib/window-information/XSetSelectionOwner.html
	 *
	 *            <pre>
	 *            The XSetSelectionOwner() function changes the owner and
	 *            last-change time for the specified selection and has no effect if
	 *            the specified time is earlier than the current last-change time of
	 *            the specified selection or is later than the current X server
	 *            time. Otherwise, the last-change time is set to the specified
	 *            time, with CurrentTime replaced by the current server time. If the
	 *            owner window is specified as None , then the owner of the
	 *            selection becomes None (that is, no owner). Otherwise, the owner
	 *            of the selection becomes the client executing the request. If the
	 *            new owner (whether a client or None ) is not the same as the
	 *            current owner of the selection and the current owner is not None ,
	 *            the current owner is sent a SelectionClear event. If the client
	 *            that is the owner of a selection is later terminated (that is, its
	 *            connection is closed) or if the owner window it has specified in
	 *            the request is later destroyed, the owner of the selection
	 *            automatically reverts to None , but the last-change time is not
	 *            affected. The selection atom is uninterpreted by the X server.
	 *            XGetSelectionOwner() returns the owner window, which is reported
	 *            in SelectionRequest and SelectionClear events. Selections are
	 *            global to the X server.
	 *
	 *            XSetSelectionOwner() can generate BadAtom and BadWindow errors.
	 *
	 *            Diagnostics BadAtom A value for an Atom argument does not name a
	 *            defined Atom. BadWindow A value for a Window argument does not
	 *            name a defined Window.
	 *
	 */
	void XSetSelectionOwner(Display display, Atom selection, Window owner, NativeLong time);

	/**
	 *
	 * @param display
	 *            Specifies the connection to the X server.
	 * @param selection
	 *            Specifies the selection atom.
	 * @param target
	 *            Specifies the target atom. Really the type or format of the
	 *            selection.
	 * @param property
	 *            Specifies the property name. You also can pass None .
	 * @param requestor
	 *            Specifies the requestor.
	 * @param time
	 *            Specifies the time. You can pass either a timestamp or
	 *            CurrentTime.
	 *
	 *            XConvertSelection() requests that the specified selection be
	 *            converted to the specified target type:
	 *            <ul>
	 *            <li>If the specified selection has an owner, the X server sends a
	 *            .PN SelectionRequest event to that owner.
	 *            <li>If no owner for the specified selection exists, the X server
	 *            generates a SelectionNotify event to the requestor with property
	 *            None.
	 *            </ul>
	 *            The arguments are passed on unchanged in either of the events.
	 *            There are two predefined selection atoms: PRIMARY and SECONDARY.
	 *
	 *            XConvertSelection() can generate BadAtom and BadWindow errors.
	 *
	 *            Diagnostics
	 *            <ul>
	 *            <li>BadAtom A value for an Atom argument does not name a defined
	 *            Atom.
	 *            <li>BadWindow A value for a Window argument does not name a
	 *            defined Window.
	 *            </ul>
	 */
	void XConvertSelection(Display display, Atom selection, Atom target, Atom property, Window requestor,
			NativeLong time);
}
