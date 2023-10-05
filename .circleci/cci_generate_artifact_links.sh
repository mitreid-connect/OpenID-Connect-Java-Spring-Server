#!/bin/bash
HOME=~/project
DOWNLOAD_PAGE=$HOME/download.html
LOG=$HOME/mavenOutput.log
SEARCH_TERMS=(openid-connect uma)

function generate_artifact_links {
    EXTENSION=$1
    echo "<!DOCTYPE html><html><body><h2>Last Deployed Artifacts</h2>" >> $DOWNLOAD_PAGE
    
    for searchTerm in ${SEARCH_TERMS[@]}; do
        jarUrls+=($(grep -Eo '(http|https).*'${searchTerm}'.*[^-sources].'${EXTENSION}' | sort --unique' $LOG))
    done

    if [[ ! -z $jarUrls ]]; then
        echo "<ul>" >> $DOWNLOAD_PAGE

        for jarUrl in "${jarUrls[@]}"; do
            jarName=$(basename $jarUrl)
            echo "<li><a href="$jarUrl">$jarName</a></li>" >> $DOWNLOAD_PAGE
        done
        echo "</ul>" >> $DOWNLOAD_PAGE
    else 
        echo "No uploaded artifacts found." >> $DOWNLOAD_PAGE
    fi
    
    echo "<h2>Last Deployed Sources</h2>" >> $DOWNLOAD_PAGE

    # get all sources upload URLs into an array.
    for searchTerm in ${SEARCH_TERMS[@]}; do
        sourceUrls+=($(grep -Eo '(http|https).*'${searchTerm}'.*[-sources].'${EXTENSION}' | sort --unique' $LOG))
    done

    #if download links are found
    if [[ ! -z $sourceUrls ]]; then
        echo "<ul>" >> $DOWNLOAD_PAGE

        # write each array entry as a list item URL
        for sourceUrl in "${sourceUrls[@]}"
        do 
            sourceName=$(basename $sourceUrl)
            echo "<li><a href="$sourceUrl">$sourceName</a></li>" >> $DOWNLOAD_PAGE
        done
        echo "</ul>" >> $DOWNLOAD_PAGE
    else
        echo "No uploaded artifacts found." >> $DOWNLOAD_PAGE
    fi
    echo "</body></html>" >> $DOWNLOAD_PAGE
}

generate_artifact_links $@ 