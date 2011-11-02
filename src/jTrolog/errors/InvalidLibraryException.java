/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jTrolog.errors;

/**
 * This exception means that a not valid tuProlog library has been specified.
 *
 * @see jTrolog.lib.Library
 */
public class InvalidLibraryException extends PrologException {

    private String libraryName;
    private PrologException rootCause;

    public InvalidLibraryException() {}

    public InvalidLibraryException(String libName, PrologException e) {
        libraryName = libName;
        rootCause = e;
    }

    public InvalidLibraryException(String libName) {
        libraryName = libName;
    }

    public String getLibraryName() {
        return libraryName;
    }

    public String getMessage() {
        return toString();
    }

    public String toString() {
        return rootCause == null ?
                "InvalidLibraryException: " + libraryName :
                "InvalidLibraryException: " + libraryName + "\nroot cause: " + rootCause;
    }

}