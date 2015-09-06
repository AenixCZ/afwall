package dev.ukanth.ufirewall.ui.about;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import dev.ukanth.ufirewall.Api;
import dev.ukanth.ufirewall.R;
import dev.ukanth.ufirewall.util.G;


public class AboutFragment extends Fragment {


	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup group,
			Bundle saved) {
		View view = inflater.inflate(R.layout.help_about_content, group, false);

		ActivitySwipeDetector swipe = new ActivitySwipeDetector();
		view.findViewById(R.id.about_thirdsparty_credits).setOnTouchListener(swipe);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PackageInfo pInfo = null;
		String version = "";
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			Log.e(Api.TAG, "Package not found", e);
		}
		version = pInfo.versionName;
		
		TextView text = (TextView) getActivity().findViewById(R.id.afwall_title);
		String versionText = getString(R.string.app_name) + " (v" + version + ")";
		if(G.isDo(getActivity().getApplicationContext()) || Api.getCurrentPackage(getActivity().getApplicationContext()).equals("dev.ukanth.ufirewall.donate")) {
			versionText = versionText + " (Donate) " +  getActivity().getString(R.string.donate_thanks)+  ":)";
		}
		text.setText(versionText);
		
		WebView creditsWebView = (WebView) getActivity().findViewById(R.id.about_thirdsparty_credits);
		try {
			String data = Api.loadData(getActivity().getBaseContext(), "about");
			creditsWebView.loadDataWithBaseURL(null, data, "text/html","UTF-8",null);
		} catch (IOException ioe) {
			Log.e(Api.TAG, "Error reading changelog file!", ioe);
		}
	}

	interface SwipeInterface {

		public void bottom2top(View v);

		public void left2right(View v);

		public void right2left(View v);

		public void top2bottom(View v);
	}


	class ActivitySwipeDetector implements View.OnTouchListener {
		static final String logTag = "ActivitySwipeDetector";
		static final int MIN_DISTANCE = 100;
		private float downX, downY, upX, upY;

		public ActivitySwipeDetector() {
		}

		public void onRightToLeftSwipe(View v) {
			Log.i(logTag, "RightToLeftSwipe!");
		}

		public void onLeftToRightSwipe(View v){
			Log.i(logTag, "LeftToRightSwipe!");
		}

		public void onTopToBottomSwipe(View v){
			Log.i(logTag, "onTopToBottomSwipe!");
		}

		public void onBottomToTopSwipe(View v){
			Log.i(logTag, "onBottomToTopSwipe!");
			Toast.makeText(getActivity(),"Swipe Works great",Toast.LENGTH_LONG).show();
		}

		public boolean onTouch(View v, MotionEvent event) {
			switch(event.getAction()){
				case MotionEvent.ACTION_DOWN: {
					downX = event.getX();
					downY = event.getY();
					return true;
				}
				case MotionEvent.ACTION_UP: {
					upX = event.getX();
					upY = event.getY();

					float deltaX = downX - upX;
					float deltaY = downY - upY;

					// swipe horizontal?
					if(Math.abs(deltaX) > MIN_DISTANCE){
						// left or right
						if(deltaX < 0) { this.onLeftToRightSwipe(v); return true; }
						if(deltaX > 0) { this.onRightToLeftSwipe(v); return true; }
					}
					else {
						Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
					}

					// swipe vertical?
					if(Math.abs(deltaY) > MIN_DISTANCE){
						// top or down
						if(deltaY < 0) { this.onTopToBottomSwipe(v); return true; }
						if(deltaY > 0) { this.onBottomToTopSwipe(v); return true; }
					}
					else {
						Log.i(logTag, "Swipe was only " + Math.abs(deltaX) + " long, need at least " + MIN_DISTANCE);
						v.performClick();
					}
				}
			}
			return false;
		}
	}
}
