package com.droid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.droid.view.MyLetterListView;
import com.droid.view.MyLetterListView.OnTouchingLetterChangedListener;
import com.droid.view.PinnedHeaderListView;


public class CheckContactsActivity extends Activity implements OnClickListener{
	private BaseAdapter adapter;  
    private PinnedHeaderListView personList;
    private TextView overlay;
    private MyLetterListView letterListView;
    private AsyncQueryHandler asyncQuery;  
    private static final String NAME = "name", NUMBER = "number", SORT_KEY = "sort_key";
    
    private HashMap<String, Integer> alphaIndexer;//存放存在的汉语拼音首字母和与之对应的列表位置
    private String[] SectionFullItem;//存放存在的汉语拼音首字母
    private List<String> mSections;
    private List<Integer> mPositions;
    private int DataSize=0;
    
    private Handler handler;
    private OverlayThread overlayThread;
    private ImageView title_back_bn;
    private ImageView title_refresh;
    
    private boolean isSearchFinished=false;
    private int mLocationPosition = -1;

  
    @Override  
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);  
     
        title_back_bn=(ImageView) this.findViewById(R.id.title_back_layout);
        title_back_bn.setOnClickListener(this);
        title_refresh=(ImageView) this.findViewById(R.id.title_refresh);
        title_refresh.setOnClickListener(this);
 
        personList = (PinnedHeaderListView) findViewById(R.id.list_view);
        personList.setPinnedHeaderView(LayoutInflater.from(this).inflate(
				R.layout.listview_head, personList, false));
        personList.setOnScrollListener(new OnScrollListener(){

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				switch(scrollState){
				case OnScrollListener.SCROLL_STATE_FLING:
					Log.i("scroll", "SCROLL_STATE_FLING");
					break;
				case OnScrollListener.SCROLL_STATE_IDLE:
					Log.i("scroll", "SCROLL_STATE_IDLE");
					break;
				case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
					Log.i("scroll", "SCROLL_STATE_TOUCH_SCROLL");
					break;
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (isSearchFinished && view instanceof PinnedHeaderListView ) {
					((PinnedHeaderListView) view).configureHeaderView(firstVisibleItem);
					Log.i("fuck","onscroll first visible position "+firstVisibleItem);
				}
			}
        	
        });
        
        
        letterListView = (MyLetterListView) findViewById(R.id.MyLetterListView01);    
        letterListView.setOnTouchingLetterChangedListener(new LetterListViewListener());
        
        asyncQuery = new MyAsyncQueryHandler(getContentResolver());
        alphaIndexer = new HashMap<String, Integer>();
        handler = new Handler();
        overlayThread = new OverlayThread();
        initOverlay();   
    }  

    @Override  
    protected void onResume() {  
        super.onResume();  
        Uri uri = Uri.parse("content://com.android.contacts/data/phones");  
        String[] projection = { "_id", "display_name", "data1", "sort_key" };  
        asyncQuery.startQuery(0, null, uri, projection, null, null,  
                "sort_key COLLATE LOCALIZED asc");
    }  
  
    private void setAdapter(List<ContentValues> list) {
    	adapter = new ListAdapter(this, list);
        personList.setAdapter(adapter);  
  
    }
    
    //初始化汉语拼音首字母弹出提示框
    private void initOverlay() {
    	LayoutInflater inflater = LayoutInflater.from(this);
    	overlay = (TextView) inflater.inflate(R.layout.overlay, null);
    	overlay.setVisibility(View.INVISIBLE);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(overlay, lp);
    }
  //获得汉语拼音首字母
    private String getAlpha(String str) {  
        if (str == null) {  
            return "#";  
        }  
  
        if (str.trim().length() == 0) {  
            return "#";  
        }  
  
        char c = str.trim().substring(0, 1).charAt(0);  
        // 正则表达式，判断首字母是否是英文字母  
        Pattern pattern = Pattern.compile("[A-Za-z]"); // "^[A-Za-z]+$"
        if (pattern.matcher(c + "").matches()) {  
            return (c + "").toUpperCase();  
        } else {  
            return "#";  
        }  
    }
    public void refresh(){
    	Uri uri = Uri.parse("content://com.android.contacts/data/phones");  
        String[] projection = { "_id", "display_name", "data1", "sort_key" };  
    	asyncQuery.startQuery(0, null, uri, projection, null, null,  
                "sort_key COLLATE LOCALIZED asc");
    }
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.title_back_layout:
			//onKeyDown(KeyEvent.KEYCODE_BACK, null);
			finish();
			break;
		case R.id.title_refresh:			
			refresh();
			break;
		}
	}

	public int getPositionForSection(int section) {
		if (section < 0 || section >= mSections.size()) {
			return -1;
		}
		return mPositions.get(section);
	}

	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		if (position < 0 || position >= DataSize) {
			return -1;
		}
		int index = Arrays.binarySearch(mPositions.toArray(), position);
		return index >= 0 ? index : -index - 2;
	}
    private class LetterListViewListener implements OnTouchingLetterChangedListener{

		@Override
		public void onTouchingLetterChanged(final String s) {
			if(alphaIndexer.get(s) != null) {
				int position = alphaIndexer.get(s);
				personList.setSelection(position);
				overlay.setText(SectionFullItem[position]);
				overlay.setVisibility(View.VISIBLE);
				handler.removeCallbacks(overlayThread);
				//延迟一秒后执行，让overlay为不可见
				handler.postDelayed(overlayThread, 1000);
			} 
		
			/*根据是否为开通啪啪奇的好友来判断*/
			if(s.equals("!")){
				int position=0;
				personList.setSelection(position);
				overlay.setText("!");
				overlay.setVisibility(View.VISIBLE);
				handler.removeCallbacks(overlayThread);
				//延迟一秒后执行，让overlay为不可见
				handler.postDelayed(overlayThread, 1000);
			}
			if(s.equals("A")){
				int position=2;
				personList.setSelection(position);
				overlay.setText("A");
				overlay.setVisibility(View.VISIBLE);
				handler.removeCallbacks(overlayThread);
				//延迟一秒后执行，让overlay为不可见
				handler.postDelayed(overlayThread, 1000);
			}
			/*根据是否为开通啪啪奇的好友来判断*/
		}
    	
    }
    
    //设置overlay不可见
    private class OverlayThread implements Runnable {

		@Override
		public void run() {
			overlay.setVisibility(View.GONE);
		}
    	
    }
    private class ListAdapter extends BaseAdapter implements PinnedHeaderAdapter{
   	 	private LayoutInflater inflater;  
        private List<ContentValues> list;
        
   	
      public ListAdapter(Context context, List<ContentValues> list) {
   		this.inflater = LayoutInflater.from(context);
   		this.list = list;
   		alphaIndexer = new HashMap<String, Integer>();
   		SectionFullItem=new String[list.size()];
   		DataSize=list.size();
   		mPositions = new ArrayList<Integer>();
   		mSections = new ArrayList<String>();
   		
   		for (int i = 0; i < list.size(); i++) {
   			//当前汉语拼音首字母
   			String currentStr = getAlpha(list.get(i).getAsString(SORT_KEY));
   			//上一个汉语拼音首字母，如果不存在为“ ”
               String previewStr = (i - 1) >= 0 ? getAlpha(list.get(i - 1).getAsString(SORT_KEY)) : " ";
               if (!previewStr.equals(currentStr)) {
               	String name = getAlpha(list.get(i).getAsString(SORT_KEY));
               	alphaIndexer.put(name, i);  
               	SectionFullItem[i] = name; 
               	mPositions.add(i);
               	mSections.add(name);
               }
           }
   		
   	}
   	
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {  
               convertView = inflater.inflate(R.layout.ugc_check_contacts_item_layout, null);
               holder = new ViewHolder();                 
               holder.type_layout=(RelativeLayout)convertView.findViewById(R.id.type_layout);                
               holder.type = (TextView) convertView.findViewById(R.id.type);
               holder.alpha_layout=(RelativeLayout)convertView.findViewById(R.id.alpha_layout);
               holder.alpha = (TextView) convertView.findViewById(R.id.alpha);            
               holder.faceIcon = (ImageView) convertView.findViewById(R.id.faceIcon); 
               holder.name = (TextView) convertView.findViewById(R.id.name); 
               holder.addImg = (ImageView) convertView.findViewById(R.id.attentionIamge); 

               convertView.setTag(holder);  
           } else {  
               holder = (ViewHolder) convertView.getTag();  
           }  
           ContentValues cv = list.get(position);  
           holder.name.setText(cv.getAsString(NAME)+position);
           holder.addImg.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Toast.makeText(CheckContactsActivity.this, list.get(position).getAsString(NAME)+position, Toast.LENGTH_SHORT).show();
					ViewHolder h=(ViewHolder) personList.getChildAt(position).getTag();
					if(h!=null)h.name.setText("haha item refresh");
				}
          	
          });
           String currentStr = getAlpha(list.get(position).getAsString(SORT_KEY));
           String previewStr = (position - 1) >= 0 ? getAlpha(list.get(position - 1).getAsString(SORT_KEY)) : " ";
           
           /*position 根据开通啪啪奇的好友数量确定*/
           	if(position==0){
              	holder.type_layout.setVisibility(View.VISIBLE);
              	holder.type.setText("已开通啪啪奇的好友");
              	convertView.setBackgroundResource(R.drawable.phone_card_style_bg_top);
              }
              else if(position==1){
            	holder.type_layout.setVisibility(View.GONE);
              	convertView.setBackgroundResource(R.drawable.phone_card_style_bg_bottom);
              	
              }else if(position==2){
              	holder.type_layout.setVisibility(View.VISIBLE);
              	holder.type.setText("未开通啪啪奇的好友");
              	convertView.setBackgroundResource(R.drawable.phone_card_style_bg_top);
              }
              else {
              	holder.type_layout.setVisibility(View.GONE);
              	convertView.setBackgroundResource(R.drawable.phone_card_style_bg_middle);
              }
           
           if (!previewStr.equals(currentStr) || position==2) {  
               holder.alpha_layout.setVisibility(View.VISIBLE);
               holder.alpha.setText(currentStr);
           } else {  
               holder.alpha_layout.setVisibility(View.GONE);
           }
           
           
           if(position==0 || position==1){
           	holder.alpha_layout.setVisibility(View.GONE);
           	FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
                       LayoutParams.WRAP_CONTENT); 
           	params.setMargins(params.leftMargin, params.topMargin, 0, params.bottomMargin); 
           	holder.addImg.setLayoutParams(params);
           }else{
           	FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
                       LayoutParams.WRAP_CONTENT); 
           	params.setMargins(params.leftMargin, params.topMargin, 10, params.bottomMargin); 
           	holder.addImg.setLayoutParams(params);
           }
             
           /*position 根据开通啪啪奇的好友数量确定*/
           
           return convertView;  
		}
		
		private class ViewHolder {			 
			RelativeLayout type_layout;			 
			TextView type; 
			RelativeLayout alpha_layout;
			TextView alpha;
			ImageView faceIcon;
            TextView name;  
            ImageView addImg;
		}

		@Override
		public int getPinnedHeaderState(int position) {
			int realPosition = position;
			if (realPosition < 0
					|| (mLocationPosition != -1 && mLocationPosition == realPosition)) {
				return PINNED_HEADER_GONE;
			}
			mLocationPosition = -1;
			int section = getSectionForPosition(realPosition);
			int nextSectionPosition = getPositionForSection(section + 1);
			/*position 根据开通啪啪奇的好友数量确定*/
			if (nextSectionPosition != -1
					&& realPosition == nextSectionPosition - 1 || position==1 ) {
				return PINNED_HEADER_PUSHED_UP;
			}
			/*position 根据开通啪啪奇的好友数量确定*/
			return PINNED_HEADER_VISIBLE;
		}
	
		@Override
		public void configurePinnedHeader(View header, int position) {
			// TODO Auto-generated method stub
			
			/*position 根据开通啪啪奇的好友数量确定*/
			if(position<2){
				((TextView) header.findViewById(R.id.friends_list_header_text)).setText("已开通啪啪奇的好友");				
				return;
			}
			/*position 根据开通啪啪奇的好友数量确定*/
			int section = getSectionForPosition(position);
			String title = (String) mSections.get(section);
			((TextView) header.findViewById(R.id.friends_list_header_text))
					.setText(title);
			
			
		}
   	
   }
  //查询联系人
    private class MyAsyncQueryHandler extends AsyncQueryHandler {  
  
        public MyAsyncQueryHandler(ContentResolver cr) {  
            super(cr);  
        }  
  
        @Override  
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
            if (cursor != null && cursor.getCount() > 0) {  
                List<ContentValues> list = new ArrayList<ContentValues>();  
                cursor.moveToFirst();  
                for (int i = 0; i < cursor.getCount(); i++) {
                    ContentValues cv = new ContentValues();  
                    cursor.moveToPosition(i);  
                    String name = cursor.getString(1);  
                    String number = cursor.getString(2);  
                    String sortKey = cursor.getString(3);
                    if (number.startsWith("+86")) {  
                        cv.put(NAME, name);  
                        cv.put(NUMBER, number.substring(3));  //去掉+86
                        cv.put(SORT_KEY, sortKey);  
                    } else {  
                        cv.put(NAME, name);  
                        cv.put(NUMBER, number);  
                        cv.put(SORT_KEY, sortKey);  
                    }  
                    if(!getAlpha(sortKey).equals("#"))list.add(cv);  
                }  
                if (list.size() > 0) {  
                    setAdapter(list);  
                }  
            }
            isSearchFinished=true;
        }  
  
    }  
}  