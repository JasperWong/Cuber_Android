package com.jasper.cuber;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;


public class MusicActivity extends FragmentActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private static final Uri SONGLIST_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final Uri ALBUM_URI = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
    private static final Uri ARTIST_URI = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

    private static final String[] PROJECTION_SONGLIST = new String[] {
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST }; // SimpleCursorAdapter的projection
    private static final String[] PROJECTION_ALBUM = new String[] {
            MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ARTIST }; // SimpleCursorAdapter的projection
    private static final String[] PROJECTION_ARTIST = new String[] { MediaStore.Audio.Artists.ARTIST }; // SimpleCursorAdapter的projection
    // seleciton 是where子句
    private static final String SELECTION_ARTISTSUB = MediaStore.Audio.Albums.ARTIST
            + "=?";
    private String[] selectionArgs_artistsub;
    // private final String[] selectionArgs_artistsub = new String[] {};
    private static final String SORT_ORDER = "TITLE"; // CursorLoader的排列顺序：按歌曲名称排列
    private static final int LOADER_SONGLIST = -3; // 装载器Loader的唯一ID，Activity/Fragment里唯一
    private static final int LOADER_ARTIST = -2;
    private static final int LOADER_ALBUM = -1;
    // private static final int LOADER_ARTISTSUB = 3;
    // The callbacks through which we will interact with the LoaderManager.
    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks;
    // The adapter that binds our data to the ListView
    public static SimpleCursorAdapter mSongListAdapter;
    public static SimpleCursorTreeAdapter mArtistListAdapter;
    public static SimpleCursorAdapter mAlbumListAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mCallbacks = this;

		/*
		 * Initialize the adapter. Note that we pass a 'null' Cursor as the
		 * third argument. We will pass the adapter a Cursor only when the data
		 * has finished loading for the first time (i.e. when the LoaderManager
		 * delivers the data to onLoadFinished). Also note that we have passed
		 * the "0" flag as the last argument. This prevents the adapter from
		 * registering a ContentObserver for the Cursor (the CursorLoader will
		 * do this for us!).
		 */
        mSongListAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null, PROJECTION_SONGLIST,
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);
        mAlbumListAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2, null, PROJECTION_ALBUM,
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);
		/*
		 * mArtistListAdapter = new SimpleCursorAdapter(this,
		 * android.R.layout.simple_expandable_list_item_1, null,
		 * projection_artist, new int[] { android.R.id.text1 }, 0);
		 */
        mArtistListAdapter = new SimpleCursorTreeAdapter(this, null,
                android.R.layout.simple_expandable_list_item_1,
                PROJECTION_ARTIST, new int[] { android.R.id.text1 },
                android.R.layout.simple_expandable_list_item_2,
                PROJECTION_ALBUM, new int[] { android.R.id.text1,
                android.R.id.text2 }) {

            @Override
            protected Cursor getChildrenCursor(Cursor groupCursor) {
                // Given the group, we return a cursor for all the children
                // within that
                // group
                final String artistGroup = groupCursor.getString(groupCursor
                        .getColumnIndex(MediaStore.Audio.Artists.ARTIST));
                Bundle bundle = new Bundle();
                bundle.putString("artistGroup", artistGroup);
                int groupPos = groupCursor.getPosition();
                Loader<Cursor> loader = getLoaderManager().getLoader(groupPos);
                if (loader != null && !loader.isReset()) {
                    getLoaderManager().restartLoader(groupPos, bundle,
                            mCallbacks);
                } else {
                    getLoaderManager().initLoader(groupPos, bundle, mCallbacks);
                }
                return null;
            }
        };
        // mGetAdapter = new GetAdapter();
        // Associate the (now empty) adapter with the ListView.

		/*
		 * Initialize the Loader with id '1' and callbacks 'mCallbacks'. If the
		 * loader doesn't already exist, one is created. Otherwise, the already
		 * created Loader is reused. In either case, the LoaderManager will
		 * manage the Loader across the Activity/Fragment lifecycle, will
		 * receive any new loads once they have completed, and will report this
		 * new data back to the 'mCallbacks' object.
		 */
        LoaderManager lm = getLoaderManager();
		/*
		 * 参数: 1、 第一个参数：0 为Loader的唯一标识ID； 2、 第二个参数： 为Loader的构造器可选参数，这里为null； 3、
		 * 第三个参数：this，这里表示当前Activity对象或者Fragment对象，提供给LoaderManager对象进行数据汇报。
		 */
        lm.initLoader(LOADER_SONGLIST, null, mCallbacks);
        lm.initLoader(LOADER_ARTIST, null, mCallbacks);
        lm.initLoader(LOADER_ALBUM, null, mCallbacks);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        switch (id) {
            case LOADER_SONGLIST:
                // 用以下的查询参数创建一个新的游标加载器CursorLoader
                return new CursorLoader(MusicActivity.this, SONGLIST_URI, null,
                        null, null, SORT_ORDER); // 返回Cursor类型
            case LOADER_ALBUM:
                return new CursorLoader(MusicActivity.this, ALBUM_URI, null, null,
                        null, null);
            case LOADER_ARTIST:
                return new CursorLoader(MusicActivity.this, ARTIST_URI, null, null,
                        null, null);
            // artist的子列表，即专辑
            default:
                String artistGroup = bundle.getString("artistGroup");
                selectionArgs_artistsub = new String[] { String
                        .valueOf(artistGroup) };
                return new CursorLoader(MusicActivity.this, ALBUM_URI, null,
                        SELECTION_ARTISTSUB, selectionArgs_artistsub, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        switch (loader.getId()) {
            // A switch-case is useful when dealing with multiple Loaders/IDs
            case LOADER_SONGLIST:
			/*
			 * The asynchronous load is complete and the data is now available
			 * for use. Only now can we associate the queried Cursor with the
			 * SimpleCursorAdapter.
			 */
                mSongListAdapter.swapCursor(cursor);
                break;
            case LOADER_ALBUM:
                mAlbumListAdapter.swapCursor(cursor);
                // cursor.getPosition();
                // mArtistListAdapter.setChildrenCursor(loader.getId(), cursor);
                break;
            case LOADER_ARTIST:
                mArtistListAdapter.setGroupCursor(cursor);
                break;
            default:
                mArtistListAdapter.setChildrenCursor(loader.getId(), cursor);
        }
        // The listview now displays the queried data.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
		/*
		 * For whatever reason, the Loader's data is now unavailable. Remove any
		 * references to the old data by replacing it with a null Cursor.
		 */
        switch (loader.getId()) {
            case LOADER_SONGLIST:
                mSongListAdapter.swapCursor(null);
                break;
            case LOADER_ALBUM:
                mAlbumListAdapter.swapCursor(null);
                // mArtistListAdapter.setChildrenCursor(loader.getId(), null);
                break;
            case LOADER_ARTIST:
                mArtistListAdapter.setGroupCursor(null);
                break;
            default:
                mArtistListAdapter.setChildrenCursor(loader.getId(), null);
        }
    }

    // 内部类
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            SectionFragment sf = new SectionFragment();
            Bundle args = new Bundle();
            args.putInt(SectionFragment.ARG_SECTION_NUMBER, position + 1);
            sf.setArguments(args);
            return sf;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_songlist);
                case 1:
                    return getString(R.string.title_artist);
                case 2:
                    return getString(R.string.title_album);
            }
            return null;
        }
    }

	/*
	 * //内部类：展开式列表适配器 public class ExpandableAdapter extends
	 * BaseExpandableListAdapter{ }
	 */

    // 内部类
    public static class SectionFragment extends Fragment {
        private ListView mSongListView;
        private ListView mAlbumListView;
        private ExpandableListView mArtistListView;
        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:																					// after selecting music
                    View song_root_view = inflater.inflate(
                            R.layout.fragment_song_list, container, false);
                    mSongListView = (ListView) song_root_view
                            .findViewById(R.id.song_list);
                    mSongListView.setAdapter(mSongListAdapter);
                    mSongListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            Cursor c = (Cursor) mSongListAdapter.getItem(position); // position是Cursor中的，id是MediaStore中的
                            int pathColumn = c
                                    .getColumnIndex(MediaStore.Audio.Media.DATA);
                            int titleColumn = c
                                    .getColumnIndex(MediaStore.Audio.Media.TITLE);
                            int artistColumn = c
                                    .getColumnIndex(MediaStore.Audio.Media.ARTIST);
                            String song_path = c.getString(pathColumn);
                            String song_title = c.getString(titleColumn);
                            String artist = c.getString(artistColumn);
                            Toast.makeText(getActivity(), "即将播放:" + song_title,
                                    Toast.LENGTH_LONG).show();
                            Intent it = new Intent(
                                    getActivity(),
                                    Control_vertical.class);
                            it.putExtra("song_title", song_title);
                            it.putExtra("song_path", song_path);
                            it.putExtra("artist", artist);
                            if(Control_vertical.Mark==1) startActivity(it);
                            if(Control_vertical.Mark==2) ;
                        }
                    });
                    return song_root_view;
                case 2:
                    View artist_root_view = inflater.inflate(
                            R.layout.fragment_artist, container, false);
                    mArtistListView = (ExpandableListView) artist_root_view
                            .findViewById(R.id.artist_list);
                    mArtistListView.setAdapter(mArtistListAdapter);
                    mArtistListView
                            .setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                                @Override
                                public boolean onChildClick(
                                        ExpandableListView parent, View v,
                                        int groupPosition, int childPosition,
                                        long id) {
                                    Cursor c = (Cursor) mArtistListAdapter
                                            .getChild(groupPosition, childPosition);
                                    int idColumn = c
                                            .getColumnIndex(MediaStore.Audio.Albums._ID);
                                    int albumColumn = c
                                            .getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                                    String album_id = c.getString(idColumn);
                                    String album_title = c.getString(albumColumn);
                                    Toast.makeText(getActivity(),
                                            "即将展示专辑:" + album_title,
                                            Toast.LENGTH_LONG).show();
//                                    Intent it = new Intent(
//                                            getActivity(),
//                                            com.liam.music.activity.AlbumSongListActivity.class);
//                                    it.putExtra("album_id", album_id);
//                                    startActivity(it);
                                    return false;
                                }
                            });
                    return artist_root_view;
                case 3:
                    View album_root_view = inflater.inflate(
                            R.layout.fragment_album, container, false);
                    mAlbumListView = (ListView) album_root_view
                            .findViewById(R.id.album_list);
                    mAlbumListView.setAdapter(mAlbumListAdapter);
                    mAlbumListView
                            .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                @Override
                                public void onItemClick(AdapterView<?> parent,
                                                        View view, int position, long id) {
                                    Cursor c = (Cursor) mAlbumListAdapter
                                            .getItem(position); // position是Cursor中的，id是MediaStore中的
                                    int idColumn = c
                                            .getColumnIndex(MediaStore.Audio.Albums._ID);
                                    int albumColumn = c
                                            .getColumnIndex(MediaStore.Audio.Albums.ALBUM);
                                    String album_id = c.getString(idColumn);
                                    String album_title = c.getString(albumColumn);
                                    Toast.makeText(getActivity(),
                                            "即将展示专辑:" + album_title,
                                            Toast.LENGTH_LONG).show();
//                                    Intent it = new Intent(
//                                            getActivity(),
//                                            com.liam.music.activity.AlbumSongListActivity.class);
//                                    it.putExtra("album_id", album_id);
//                                    startActivity(it);
                                }
                            });
                    return album_root_view;
            }
            return null;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_music, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
