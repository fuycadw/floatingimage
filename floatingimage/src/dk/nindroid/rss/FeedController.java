package dk.nindroid.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import android.util.Log;
import dk.nindroid.rss.data.FeedReference;
import dk.nindroid.rss.data.ImageReference;
import dk.nindroid.rss.flickr.FlickrFeeder;
import dk.nindroid.rss.parser.FeedParser;
import dk.nindroid.rss.parser.ParserProvider;
import dk.nindroid.rss.settings.FeedsDbAdapter;
import dk.nindroid.rss.settings.Settings;

public class FeedController {
	private List<List<ImageReference>> 		mReferences;
	private List<Integer>					mFeedIndex;
	private List<FeedReference>				mFeeds;
	private int nextFeed = 0;
	
	public FeedController(){
		mFeeds = new ArrayList<FeedReference>();
		mFeedIndex = new ArrayList<Integer>();
		mReferences = new ArrayList<List<ImageReference>>();
	}
	
	public ImageReference getImageReference(){
		ImageReference ir = null;
		if(mReferences.size() == 0) return null;
		List<ImageReference> feed = mReferences.get(nextFeed); 
		int index = mFeedIndex.get(nextFeed);
		ir = feed.get(index);
		mFeedIndex.set(nextFeed, (index + 1) % feed.size());
		nextFeed = (nextFeed + 1) % mReferences.size();
		return ir;
	}
	
	public void readFeeds(){
		mFeeds.clear();
		mFeedIndex.clear();
		if(Settings.useRandom){
			mFeeds.add(getFeedReference(FlickrFeeder.getExplore(), Settings.TYPE_FLICKR, "Explore"));
		}
		// DB is not current!
		FeedsDbAdapter mDbHelper = new FeedsDbAdapter(ShowStreams.current);
		/*
		mDbHelper.open();
		Cursor c = null;
		try{
			c = mDbHelper.fetchAllFeeds();
			while(c.moveToNext()){
				int type = c.getInt(1); 
				if(type != Settings.TYPE_LOCAL){
					String feed = c.getString(2);
					String name = "UNKNOWN";// c.getString(3);
					
					// Only add a single feed once!
					if(!mFeeds.contains(feed)){
						mFeeds.add(getFeedReference(feed, type, name));
					}
				}
			}
		}catch(Exception e){
			Log.e("Local feeder", "Unhandled exception caught", e);
		}finally{
			if(c != null){
				c.close();
				mDbHelper.close();
			}
		}
		*/
		parseFeeds();
	}
	
	private FeedParser getParser(int feedType){
		if(feedType == Settings.TYPE_LOCAL){
			return null;
		}
		return ParserProvider.getParser(feedType);
	}
	
	public boolean showFeed(FeedReference feed){
		mFeeds.clear();
		if(feed.getParser() != null){
			mFeeds.add(feed);
			return parseFeeds();
		}
		return false;
	}
	
	public FeedReference getFeedReference(String path, int type, String name){
		return new FeedReference(getParser(type), path, name);
	}
	
	// False if no images.
	private boolean parseFeeds(){
		mReferences.clear();
		for(FeedReference feed : mFeeds){
			int i = 5;
			List<ImageReference> reference = null;
			while(i-->0){
				try{
					reference = parseFeed(feed);
					break;
				}catch (Exception e){
					Log.w("FeedController", "Failed getting feed, retrying...");
				}
			}
			if(reference != null){
				mReferences.add(reference); // These two 
				mFeedIndex.add(0);			// are in sync!
			}else{
				Log.w("FeedController", "Reading feed failed too many times, giving up!");
			}
		}
		return mReferences.size() > 0;
	}
	
	private static List<ImageReference> parseFeed(FeedReference feed){
		try {
			// Explore //InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.interestingness.getList&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500");
			// Mine    //InputStream stream = HttpTools.openHttpConnection("http://api.flickr.com/services/rest/?method=flickr.people.getPublicPhotos&api_key=f6fdb5a636863d148afa8e7bb056bf1b&per_page=500&user_id=73523270@N00");
			InputStream stream = HttpTools.openHttpConnection(feed.getFeedLocation());
			Log.v("FeedController", "Fetching stream: " + feed.getFeedLocation());
			return parseStream(stream, feed.getParser());
		} catch (IOException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (ParserConfigurationException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (SAXException e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		} catch (FactoryConfigurationError e) {
			Log.e("FeedController", "Unexpected exception caught", e);
		}
		return null;
	}
	
	private static List<ImageReference> parseStream(InputStream stream, FeedParser feedParser) throws ParserConfigurationException, SAXException, FactoryConfigurationError, IOException{
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		XMLReader xmlReader = parser.getXMLReader();
		xmlReader.setContentHandler(feedParser);
		xmlReader.parse(new InputSource(stream));
		List<ImageReference> list = feedParser.getData();
		if(list != null){
			if(list.isEmpty()){
				return null;
			}
			Log.v("FeedController", list.size() + " photos found.");
		}
		return list;
	}
}