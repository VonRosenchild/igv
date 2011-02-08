package org.broad.igv.gwas;

import org.apache.log4j.Logger;
import org.broad.igv.util.ParsingUtils;

import java.util.ArrayList;


/**
 * Created by IntelliJ IDEA.
 * User: jussi
 * Date: 2/7/11
 * Time: 1:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class DescriptionCache {

    private static final Logger log = Logger.getLogger(DescriptionCache.class);


    // Maximum amount of values stored in the cache
    private int maxSize = 10000;

    private ArrayList<String> chrs = new ArrayList<String>();
    private ArrayList<Integer> locations = new ArrayList<Integer>();
    private ArrayList<String> descriptions = new ArrayList<String>();
    // Storage for the header tokens
    private String[] headerTokens = new String[1000];


    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public String[] getHeaderTokens() {
        return headerTokens;
    }

    public void setHeaderTokens(String[] headerTokens) {
        this.headerTokens = headerTokens;
    }

    public void clear() {

        this.chrs = new ArrayList<String>();
        this.locations = new ArrayList<Integer>();
        this.descriptions = new ArrayList<String>();
    }


    public boolean add(String chr, int location, String description) {

        if (locations.size() >= maxSize) {
            locations.remove(0);
            chrs.remove(0);
            descriptions.remove(0);
        }

        return chrs.add(chr) & locations.add(location) & descriptions.add(description);
    }

    public String getDescription(String chr, int location) {

        String description = null;

        int indexCounter = 0;
        boolean descriptionFound = false;

        while (indexCounter < this.descriptions.size() && !descriptionFound) {
            if (this.chrs.get(indexCounter).equals(chr) && this.locations.get(indexCounter) == location) {
                description = this.descriptions.get(indexCounter);
                descriptionFound = true;

            }
            indexCounter++;
        }

        return description;

    }


    public String getDescriptionString(String chr, int location) {

        String description = this.getDescription(chr, location);
        String descriptionString = null;

        if (description != null) {
            descriptionString = "";
            int headersSize = this.getHeaderTokens().length;
            String[] tokens = new String[1000];

            ParsingUtils.splitSpaces(description, tokens);

            for (int i = 0; i < headersSize; i++) {
                String tmpHeaderToken = this.getHeaderTokens()[i];
                if (tmpHeaderToken != null)
                    descriptionString += tmpHeaderToken + ": " + tokens[i] + "<br>";
            }

        }
        return descriptionString;

    }


}
