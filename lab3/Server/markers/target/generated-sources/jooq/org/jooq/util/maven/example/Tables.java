/*
 * This file is generated by jOOQ.
*/
package org.jooq.util.maven.example;


import javax.annotation.Generated;

import org.jooq.util.maven.example.tables.Marker;
import org.jooq.util.maven.example.tables.Photo;


/**
 * Convenience access to all tables in public
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.8"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>public.marker</code>.
     */
    public static final Marker MARKER = org.jooq.util.maven.example.tables.Marker.MARKER;

    /**
     * The table <code>public.photo</code>.
     */
    public static final Photo PHOTO = org.jooq.util.maven.example.tables.Photo.PHOTO;
}