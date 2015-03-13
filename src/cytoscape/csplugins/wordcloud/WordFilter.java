/*
 File: WordFilter.java

 Copyright 2010 - The Cytoscape Consortium (www.cytoscape.org)
 
 Code written by: Layla Oesper
 Authors: Layla Oesper, Ruth Isserlin, Daniele Merico
 
 This library is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this project.  If not, see <http://www.gnu.org/licenses/>.
 */

package cytoscape.csplugins.wordcloud;

import cytoscape.data.readers.TextFileReader;
import cytoscape.data.readers.TextJarReader;

import java.io.File;

import java.net.URISyntaxException;
import java.net.URL;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;



/**
 * This class defines the WordFilter class.  This class is used to determine
 * if a word in question should be filtered out.  The list of words that will
 * be filtered is built from included .txt files.
 * 
 * @author Layla Oesper
 * @version 1.0
 */


public class WordFilter 
{
	
	//VARIABLES
	
	private HashSet<String> stopWords = new HashSet<String>(); //Stop words
	private HashSet<String> flaggedWords = new HashSet<String>();//Flagged words
	private HashSet<String> addedWords = new HashSet<String>(); //User added words
	private HashSet<String> numberWords = new HashSet<String>();
	private Boolean filterNums = false;
	final static public String stopWordFile = "StopWords.txt";
	final static public String flaggedWordFile = "FlaggedWords.txt";
	final static public String separator = "/";
	final static public String resources = "resources";
	final static public String newline = "\n";
	
	//String Delimeters
	private static final String DELIMITER = "WordFilterDelimiter";
	
	
	//CONSTRUCTORS
	
	/**
	 * WordFilter constructor
	 * @throws FileNotFoundException 
	 */
	public WordFilter()
	{
		//Initialize from files
		String stopPath = resources + separator + stopWordFile;
		String flaggedPath = resources + separator + flaggedWordFile;
		
		this.initialize(stopPath, stopWords);
		this.initialize(flaggedPath, flaggedWords);
		this.initializeNums();
	}
	
	/**
	 * Constructor to create WordFilter from a cytoscape property file
	 * while restoring a session.  Property file is created when the session is saved.
	 * @param propFile - the contents of the property file as a String
	 */
	public WordFilter(String propFile)
	{
		
		//Create a hashmap to contain all the values in the rpt file
		HashMap<String, String> props = new HashMap<String,String>();
		
		String[] lines = propFile.split("\n");
		
		for (int i = 0; i < lines.length; i++)
		{
			String line = lines[i];
			String[] tokens = line.split("\t");
			//there should be two values in each line
			if(tokens.length == 2)
				props.put(tokens[0],tokens[1]);
		}
		
		//Rebuild Stop List
		String value = props.get("StopWords");
		if (value != null)
		{
			String[] words = value.split(DELIMITER);
			for (int i = 0; i < words.length; i++)
			{
				String curWord = words[i];
				stopWords.add(curWord);
			}
		}
		
		//Rebuild flagged List
		value = props.get("FlaggedWords");
		if (value != null)
		{
			String[] words = value.split(DELIMITER);
			for (int i = 0; i < words.length; i++)
			{
				String curWord = words[i];
				flaggedWords.add(curWord);
			}
		}
		
		//Rebuild added List
		value = props.get("AddedWords");
		if (value != null)
		{
			String[] words = value.split(DELIMITER);
			for (int i = 0; i < words.length; i++)
			{
				String curWord = words[i];
				addedWords.add(curWord);
			}
		}
		
		//Rebuild number list
		this.initializeNums();
		value = props.get("FilterNums");
		if (value != null)
		{
			this.filterNums = Boolean.parseBoolean(value);
		}
	}
	
	
	//METHODS
	
	/**
	 * Checks to see if the word should be filtered
	 * 
	 * @param aWord - word to be checked
	 * @return boolean - true if word should be filtered out
	 */
	public boolean contains(String aWord)
	{
		if (stopWords.contains(aWord))
			return true;
		else if (flaggedWords.contains(aWord))
			return true;
		else if (addedWords.contains(aWord))
			return true;
		else if (filterNums)
		{
			if (numberWords.contains(aWord))
				return true;
			else
				return false;
		}
		else
			return false;
	}
	
	/**
	 * Adds a word to the list of words to filter.
	 * 
	 * @param aWord - word to be added to filter
	 */
	public void add(String aWord)
	{
		addedWords.add(aWord);
	}
	
	/**
	 * Removes a word from the list of words to filter.
	 * 
	 * @param aWord - word to be removed from the filter.
	 */
	public void remove(String aWord)
	{
		if (stopWords.contains(aWord))
			stopWords.remove(aWord);
		else if (flaggedWords.contains(aWord))
			flaggedWords.remove(aWord);
		else if (addedWords.contains(aWord))
			addedWords.remove(aWord);	
	}
	
	/**
	 * Initializes the WordFilter to contain words from the specified
	 * resource file.
	 * 
	 * @param resourcePath - location, relative to this class of the .txt file
	 * containing list of words to add to this filter.
	 */
	private void initialize(String resourcePath, HashSet<String> wordSet)
	{
		URL myURL = SemanticSummaryPlugin.class.getResource(resourcePath);
		
		try
		{
			//Read file and retrieve all lines
			TextJarReader reader = new TextJarReader(myURL.toString());
			reader.read();
			String fullText = reader.getText();
		
			StringTokenizer token = new StringTokenizer(fullText);
			while(token.hasMoreTokens())
			{
				String curWord = token.nextToken();
				wordSet.add(curWord);
			}

		}
		catch (Exception e)
		{
			//If we are running not from a jar file
			File f;
			try
			{
				f = new File(myURL.toURI());
			}
			catch (URISyntaxException ex)
			{
				f = new File(myURL.getPath());
			}
			
			String path = f.getAbsolutePath();
			
			//Read file and retrieve all lines
			TextFileReader reader = new TextFileReader(path);
			reader.read();
			String fullText = reader.getText();
		
			String[] lines = fullText.split("\n");
		
			//Each line should be a word, add to filter
			for (int i = 0; i < lines.length; i++)
			{
				String curWord = lines[i];
				wordSet.add(curWord);
			}//end for loop
		}
		
	}
	
	/**
	 * Creates a String representation of all words currently in this 
	 * WordFilter.
	 * 
	 * @return String - list of words in this WordFilter.
	 */
	
	public String toString()
	{
		StringBuffer filterVariables = new StringBuffer();
		
		//List of stop words as a delimited list
		StringBuffer output = new StringBuffer();
		if (stopWords.size()> 0)
		{
			for (Iterator<String> iter = stopWords.iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				output.append(curWord + DELIMITER);
			}
			filterVariables.append("StopWords\t" + output.toString() + "\n");
		}
		
		//List of flagged words as a delimeted list
		output = new StringBuffer();
		if (flaggedWords.size()> 0)
		{
			for (Iterator<String> iter = flaggedWords.iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				output.append(curWord + DELIMITER);
			}
			filterVariables.append("FlaggedWords\t" + output.toString() + "\n");
		}
		
		//List of flagged words as a delimeted list
		output = new StringBuffer();
		if (addedWords.size()>0)
		{
			for (Iterator<String> iter = addedWords.iterator(); iter.hasNext();)
			{
				String curWord = iter.next();
				output.append(curWord + DELIMITER);
			}
			filterVariables.append("AddedWords\t" + output.toString() + "\n");
		}
		
		filterVariables.append("FilterNums\t" + filterNums + "\n");
		
		return filterVariables.toString();
	}
	
	/**
	 * Builds the hashmap that contains the string values of the numbers 0
	 * to 999 to check for exclusion if this option is set to true.
	 */
	private void initializeNums()
	{
		//Clears old values
		numberWords = new HashSet<String>();
		
		for (int i = 0; i < 1000; i++)
		{
			String value = Integer.toString(i);
			numberWords.add(value);
		}
	}
	
	
	//Getters and Setters
	public HashSet<String> getStopWords()
	{
		return stopWords;
	}
	
	public HashSet<String> getFlaggedWords()
	{
		return flaggedWords;
	}
	
	public HashSet<String> getAddedWords()
	{
		return addedWords;
	}
	
	public HashSet<String> getNumWords()
	{
		return numberWords;
	}
	
	public Boolean getFilterNums()
	{
		return filterNums;
	}
	
	public void setFilterNums(Boolean val)
	{
		filterNums = val;
	}
}
