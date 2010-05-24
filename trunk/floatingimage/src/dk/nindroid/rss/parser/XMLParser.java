package dk.nindroid.rss.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import dk.nindroid.rss.HttpTools;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;

public abstract class XMLParser extends DefaultHandler implements FeedParser {
	public List<ImageReference> parseFeed(FeedReference feed) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException {
		InputStream stream = HttpTools.openHttpConnection(feed.getFeedLocation());
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(this);
		xmlReader.parse(new InputSource(stream));
		List<ImageReference> list = getData();
		
		if(list != null){
			if(list.isEmpty()){
				return null;
			}
			Log.v("FeedController", list.size() + " photos found.");
		}
		return list;
	}
	
	protected abstract List<ImageReference> getData();
}
