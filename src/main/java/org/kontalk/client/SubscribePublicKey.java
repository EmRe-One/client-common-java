/*
 * Kontalk client common library
 * Copyright (C) 2014 Kontalk Devteam <devteam@kontalk.org>

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.kontalk.client;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.jivesoftware.smack.util.Base64;
import org.xmlpull.v1.XmlPullParser;


/** Packet extension for presence subscription requests with public key. */
public class SubscribePublicKey implements PacketExtension {
    public static final String ELEMENT_NAME = "pubkey";
    public static final String NAMESPACE = "urn:xmpp:pubkey:2";

    /** Public key data. */
    private final byte[] mKey;
    /** Public key fingerprint. */
    private final String mFingerprint;

    /** Base64-encoded public key (cached). */
    private String mEncodedKey;

    public SubscribePublicKey(String keydata) {
        this(Base64.decode(keydata), null);
        mEncodedKey = keydata;
    }

    public SubscribePublicKey(byte[] keydata) {
        this(keydata, null);
    }

    public SubscribePublicKey(String keydata, String fingerprint) {
        this(Base64.decode(keydata), fingerprint);
        mEncodedKey = keydata;
    }

    public SubscribePublicKey(byte[] keydata, String fingerprint) {
        mKey = keydata;
        mFingerprint = fingerprint;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    public String getFingerprint() {
        return mFingerprint;
    }

    public byte[] getKey() {
        return mKey;
    }

    @Override
    public String toXML() {
        if (mEncodedKey == null)
            mEncodedKey = Base64.encodeBytes(mKey, Base64.DONT_BREAK_LINES);

        StringBuilder buf = new StringBuilder("<")
            .append(ELEMENT_NAME)
            .append(" xmlns=\"")
            .append(NAMESPACE)
            .append("\"><key>")
            .append(mEncodedKey)
            .append("</key>");

        if (mFingerprint != null)
            buf.append("<print>")
                .append(mFingerprint)
                .append("</print>");

        buf.append("</")
            .append(ELEMENT_NAME)
            .append('>');

        return buf.toString();
    }

    public static class Provider implements PacketExtensionProvider {

        @Override
        public PacketExtension parseExtension(XmlPullParser parser) throws Exception {
            String key = null, print = null;
            boolean in_key = false, in_print = false, done = false;

            while (!done) {
                int eventType = parser.next();

                if (eventType == XmlPullParser.START_TAG)
                {
                    if ("key".equals(parser.getName()))
                        in_key = true;
                    else if ("print".equals(parser.getName()))
                        in_print = true;
                }
                else if (eventType == XmlPullParser.END_TAG)
                {
                    if ("key".equals(parser.getName()))
                        in_key = false;
                    else if ("print".equals(parser.getName()))
                        in_print = false;
                    else if (ELEMENT_NAME.equals(parser.getName()))
                        done = true;
                }
                else if (eventType == XmlPullParser.TEXT) {
                    if (in_key)
                        key = parser.getText();
                    else if (in_print)
                        print = parser.getText();
                }
            }

            if (key != null && print != null)
                return new SubscribePublicKey(key, print);
            else
                return null;

        }
    }

}
