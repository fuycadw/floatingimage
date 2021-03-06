package dk.nindroid.rss.renderers.floating.positionControllers;

import java.util.Random;

import dk.nindroid.rss.Display;
import dk.nindroid.rss.MainActivity;
import dk.nindroid.rss.gfx.Vec3f;
import dk.nindroid.rss.renderers.floating.FloatingRenderer;

public class FloatUp extends GridController {
	public enum Pos {
		UP, MIDDLE, DOWN
	};
	
	public static final float  	mFloatZ = -3.5f;
	
	MainActivity mActivity;
	Display mDisplay;
	Random mRand;
	float mXLayerPos = 0;
	
	// Return types - avoid creating new objects
	Vec3f mJitter;
	Vec3f mPos;
	float mRotation;
	private final static Vec3f ROTATION = new Vec3f(0, 0, 1);
	
	
	public FloatUp(MainActivity activity, Display display, int image, FeedDataProvider dataProvider){
		super(image, dataProvider);
		mRand = new Random(System.currentTimeMillis() + image);
		this.mDisplay = display;
		this.mActivity = activity;
		mJitter = new Vec3f();
		mPos = new Vec3f();
		switch(image % 3){
			case 0: mXLayerPos = 0.0f; break;	
			case 1: mXLayerPos = 1.25f; break;
			case 2: mXLayerPos = -1.25f; break;
		}
		jitter();
	}
	
	@Override
	public void jitter() {
		mJitter.setX(mRand.nextFloat() * FloatingRenderer.mJitterX * 2 - FloatingRenderer.mJitterX);
		mJitter.setY(mRand.nextFloat() * FloatingRenderer.mJitterY * 2 - FloatingRenderer.mJitterY);
		mJitter.setZ(mRand.nextFloat() * FloatingRenderer.mJitterZ * 2 - FloatingRenderer.mJitterZ);
		mRotation = mActivity.getSettings().rotateImages ? mRand.nextFloat() * 20.0f - 10.0f : 0;
	}

	@Override
	public float getOpacity(float interval) {
		return 1;
	}

	@Override
	public Vec3f getPosition(float interval) {
		float farBottom = getFarBottom();
		mPos.setX(mXLayerPos * mDisplay.getWidth() + mJitter.getX());
		mPos.setY(-farBottom + (interval * farBottom * 2) + mJitter.getY());
		mPos.setZ(mFloatZ + mJitter.getZ());
		return mPos;
	}


	@Override
	public void getRotation(float interval, Rotation a, Rotation b) {
		a.setX(ROTATION.getX());
		a.setY(ROTATION.getY());
		a.setZ(ROTATION.getZ());
		a.setAngle(mRotation);
		b.setAngle(0.0f);
	}

	public float getFarBottom(){
		return mDisplay.getHeight() * 0.7f * (-mFloatZ + FloatingRenderer.mJitterZ) * 1.2f + 1.3f + FloatingRenderer.mJitterY;
	}

	@Override
	public void getGlobalOffset(float x, float y, Vec3f out) {
		if(x*x*x*x > y*y){
			out.setX(-x / 100.0f);
		}
	}

	@Override
	public float getTimeAdjustment(float speedX, float speedY) {
		return -speedY;
	}
}
