/*
 * Copyright (c) 2012, the Last.fm Java Project and Committers
 * All rights reserved.
 *
 * Redistribution and use of this software in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the
 *   following disclaimer in the documentation and/or other
 *   materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.andrew.apolloMod.helpers.lastfm;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.XMLFormatter;

import com.andrew.apolloMod.helpers.DomElement;
import com.andrew.apolloMod.helpers.utils.StringUtilities;

/**
 * Bean that contains artist information.<br/>
 * This class contains static methods that executes API methods relating to
 * artists.<br/>
 * Method names are equivalent to the last.fm API method names.
 * 
 * @author Janni Kovacs
 */
public class Artist extends MusicEntry {

    public static List<DomElement> jambalista=new ArrayList<DomElement>();

    static final ItemFactory<Artist> FACTORY = new ArtistFactory();

    protected Artist(String name, String url) {
        super(name, url);
    }

    protected Artist(String name, String url, String mbid, int playcount, int listeners,
            boolean streamable) {
        super(name, url, mbid, playcount, listeners, streamable);
    }

    /**
     * Get {@link Image}s for this artist in a variety of sizes.
     * 
     * @param artistOrMbid The artist name in question
     * @param apiKey A Last.fm API key
     * @return a list of {@link Image}s
     */
    public static PaginatedResult<Image> getImages(String artistOrMbid, String apiKey) {
        return getImages(artistOrMbid, -1, -1, apiKey);
    }

    /**
     * Get {@link Image}s for this artist in a variety of sizes.
     * 
     * @param artistOrMbid The artist name in question
     * @param page Which page of limit amount to display
     * @param limit How many to return. Defaults and maxes out at 50
     * @param apiKey A Last.fm API key
     * @return a list of {@link Image}s
     */
    public static PaginatedResult<Image> getImages(String artistOrMbid, int page, int limit,
            String apiKey) {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtilities.isMbid(artistOrMbid)) {
            params.put("mbid", artistOrMbid);
        } else {
            params.put("artist", artistOrMbid);
        }
        if (page != -1) {
        	params.put("page", Integer.toString(page));
		}
        if (limit != -1) {
        	params.put("limit", Integer.toString(limit));
		}     
        Result result = Caller.getInstance().call("artist.search", apiKey, params);
        return ResponseBuilder.buildPaginatedResult(result, Image.class);
    }

    public static String pillarImagenes(String artistOrMbid, int page, int limit, String apiKey) {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtilities.isMbid(artistOrMbid)) {
            params.put("mbid", artistOrMbid);
        } else {
            params.put("artist", artistOrMbid);
        }
        if (page != -1) {
            params.put("page", Integer.toString(page));
        }
        if (limit != -1) {
            params.put("limit", Integer.toString(limit));
        }
        Result result = Caller.getInstance().call("artist.search", apiKey, params);

        jambalista=new ArrayList<DomElement>();
        procesarResultado(result.getContentElement());

        String url="";

        url=pillarLaMasGrande();

        Log.d("pausa","pausa");

        return url;
    }

    public static String pillarLaMasGrande()
    {
        String url="";
        for(int i=0;i<jambalista.size();i++)
        {
            if(jambalista.get(i).hasAttribute("size"))
            {
                if(jambalista.get(i).getAttribute("size").equalsIgnoreCase("mega"))
                {
                    return jambalista.get(i).getText();
                }
            }
        }

        return url;
    }

    public static void procesarResultado(DomElement element){

        if(element.getChildren().size()<=0)
        {
            if(element.getTagName().equalsIgnoreCase("image"))
            {
                try {
                    jambalista.add(element);
                }
                catch (Exception e)
                {

                }
            }
        }
        else
        {
            Collection<DomElement> children = element.getChildren();
            for (DomElement hijopuñetero : children) {
                procesarResultado(hijopuñetero);
            }
        }
    }

    private static class ArtistFactory implements ItemFactory<Artist> {
        @Override
        public Artist createItemFromElement(DomElement element) {
            Artist artist = new Artist(null, null);
            MusicEntry.loadStandardInfo(artist, element);
            return artist;
        }
    }
}
