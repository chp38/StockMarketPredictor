/*
 * LingPipe v. 3.9
 * Copyright (C) 2003-2010 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.corpus.parsers;

import com.aliasi.corpus.Handler;
import com.aliasi.corpus.XMLParser;

import com.aliasi.xml.DelegatingHandler;
import com.aliasi.xml.TextAccumulatorHandler;

import org.xml.sax.helpers.DefaultHandler;

/**
 * A <code>MedlineTextParser</code> extracts all text from the
 * abstracts of MEDLINE citations, passing them to the contained text
 * handler.
 *
 * <p>See {@link com.aliasi.medline.MedlineCitationSet} for more information on
 * the MEDLINE corpus format.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.1
 * @deprecated This class will move to the demos in 4.0.
 */
@SuppressWarnings("deprecation") @Deprecated
public class MedlineTextParser extends XMLParser<com.aliasi.corpus.TextHandler> {

    /**
     * Construct a text parser that extracts abstract text from
     * MEDLINE citations with a <code>null</code> handler.
     * The text handler may be set later using {@link
     * #setHandler(Handler)}.
     */
    public MedlineTextParser() {
        super();
    }

    /**
     * Construct a MEDLINE text parser with the specified handler.
     * The handler may be reset later using {@link
     * #setHandler(Handler)}.
     *
     * @param handler Text handler for this parser.
     * @deprecated See class documentation.
     */
    @Deprecated
    public MedlineTextParser(com.aliasi.corpus.TextHandler handler) {
        super(handler);
    }

    /**
     * Returns the text handler for this parser.
     *
     * @return The text handler for this parser.
     * @deprecated Use generic {@link #getHandler()} instead.
     * @throws ClassCastException If the handler      * class.
     * @deprecated see class doc
     */
    @Deprecated
    public com.aliasi.corpus.TextHandler getTextHandler() {
        return getHandler();
    }

    /**
     * Returns the XML handler for this text parser.
     */
    @Override
    public DefaultHandler getXMLHandler() {
        return new XMLHandler(getHandler());
    }

    /**
     * @deprecated suppress
     */
    @SuppressWarnings("deprecation") @Deprecated
    private static class XMLHandler extends DelegatingHandler {
        com.aliasi.corpus.TextHandler mTextHandler;
        AbstractHandler mAbstractHandler = new AbstractHandler();
        /**
         * @deprecated suppress
         */
        @SuppressWarnings("deprecation") @Deprecated
        XMLHandler(com.aliasi.corpus.TextHandler textHandler) {
            mTextHandler = textHandler;
            setDelegate(com.aliasi.medline.MedlineCitationSet.ABSTRACT_ELT,
                        mAbstractHandler);
        }
        @Override
        public void finishDelegate(String qName, DefaultHandler handler) {
            if (qName.equals(com.aliasi.medline.MedlineCitationSet.ABSTRACT_ELT)) {
                String text = mAbstractHandler.getText();
                String strippedText
                    = com.aliasi.medline.Abstract.textWithoutTruncationMarker(text);
                char[] cs = strippedText.toCharArray();
                mTextHandler.handle(cs,0,cs.length);
            }
        }
    }

    private static class AbstractHandler extends DelegatingHandler {
        TextAccumulatorHandler mTextAccumulator
            = new TextAccumulatorHandler();
        public AbstractHandler() {
            setDelegate(com.aliasi.medline.MedlineCitationSet.ABSTRACT_TEXT_ELT,
                        mTextAccumulator);
        }
        @Override
        public void startDocument() {
            mTextAccumulator.reset();
        }
        String getText() {
            return mTextAccumulator.getText();
        }
    }

}
