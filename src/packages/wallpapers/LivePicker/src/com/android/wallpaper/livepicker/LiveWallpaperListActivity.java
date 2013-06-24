/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.wallpaper.livepicker;

import android.app.ListActivity;
import android.app.WallpaperInfo;
import android.os.Bundle;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ComponentInfo;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.service.wallpaper.WallpaperService;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.text.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.io.IOException;
import java.text.Collator;

import org.xmlpull.v1.XmlPullParserException;

public class LiveWallpaperListActivity extends ListActivity implements AdapterView.OnItemClickListener {
    private static final String LOG_TAG = "LiveWallpapersPicker";

    private static final int REQUEST_PREVIEW = 100;
    
    private PackageManager mPackageManager;

    private ArrayList<Drawable> mThumbnails;
    private ArrayList<WallpaperInfo> mWallpaperInfos;
    private ArrayList<Intent> mWallpaperIntents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_wallpaper_list);

        mPackageManager = getPackageManager();

        findLiveWallpapers();

        setListAdapter(new LiveWallpapersAdapter());
        getListView().setOnItemClickListener(this);
    }

    // TODO: THIS SHOULD HAPPEN IN AN ASYNCTASK
    private void findLiveWallpapers() {
        List<ResolveInfo> list = mPackageManager.queryIntentServices(
                new Intent(WallpaperService.SERVICE_INTERFACE),
                PackageManager.GET_META_DATA);
        
        int listSize = list.size();

        mThumbnails = new ArrayList<Drawable>(listSize);
        mWallpaperIntents = new ArrayList<Intent>(listSize);
        mWallpaperInfos = new ArrayList<WallpaperInfo>(listSize);

        Resources res = getResources();
        Drawable galleryIcon = res.getDrawable(R.drawable.livewallpaper_placeholder);
        
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        paint.setTextAlign(Paint.Align.CENTER);

        Canvas canvas = new Canvas();

        Collections.sort(list, new Comparator<ResolveInfo>() {
            final Collator mCollator;

            {
                mCollator = Collator.getInstance();                    
            }

            public int compare(ResolveInfo info1, ResolveInfo info2) {
                return mCollator.compare(info1.loadLabel(mPackageManager),
                        info2.loadLabel(mPackageManager));
            }
        });

        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            ComponentInfo ci = resolveInfo.serviceInfo;
            WallpaperInfo info;
            try {
                info = new WallpaperInfo(this, resolveInfo);
            } catch (XmlPullParserException e) {
                Log.w(LOG_TAG, "Skipping wallpaper " + ci, e);
                continue;
            } catch (IOException e) {
                Log.w(LOG_TAG, "Skipping wallpaper " + ci, e);
                continue;
            }

            String packageName = info.getPackageName();
            String className = info.getServiceName();

            Intent intent = new Intent(WallpaperService.SERVICE_INTERFACE);
            intent.setClassName(packageName, className);

            mWallpaperIntents.add(intent);
            mWallpaperInfos.add(info);

            Drawable thumb = info.loadThumbnail(mPackageManager);
            if (thumb == null) {
                int thumbWidth = res.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_width);
                int thumbHeight = res.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_height);

                Bitmap thumbnail = Bitmap.createBitmap(thumbWidth, thumbHeight,
                        Bitmap.Config.ARGB_8888);

                paint.setColor(res.getColor(R.color.live_wallpaper_thumbnail_background));
                canvas.setBitmap(thumbnail);
                canvas.drawPaint(paint);

                galleryIcon.setBounds(0, 0, thumbWidth, thumbHeight);
                ((BitmapDrawable) galleryIcon).setGravity(Gravity.CENTER);
                galleryIcon.draw(canvas);

                String title = info.loadLabel(mPackageManager).toString();

                paint.setColor(res.getColor(R.color.live_wallpaper_thumbnail_text_color));
                paint.setTextSize(
                        res.getDimensionPixelSize(R.dimen.live_wallpaper_thumbnail_text_size));

                canvas.drawText(title, (int) (thumbWidth * 0.5),
                        thumbHeight - res.getDimensionPixelSize(
                                R.dimen.live_wallpaper_thumbnail_text_offset), paint);

                thumb = new BitmapDrawable(res, thumbnail);
            }

            thumb.setDither(true);
            mThumbnails.add(thumb);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PREVIEW) {
            if (resultCode == RESULT_OK) finish();
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Intent intent = mWallpaperIntents.get(position);
        final WallpaperInfo info = mWallpaperInfos.get(position);
        LiveWallpaperPreview.showPreview(this, REQUEST_PREVIEW, intent, info);
    }

    static class ViewHolder {
        TextView titleAuthor;
        TextView description;
        ImageView thumbnail;
    }

    private class LiveWallpapersAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;

        LiveWallpapersAdapter() {
            mInflater = LayoutInflater.from(LiveWallpaperListActivity.this);
        }

        public int getCount() {
            return mWallpaperInfos.size();
        }

        public Object getItem(int position) {
            return mWallpaperInfos.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.live_wallpaper_entry, parent, false);

                holder = new ViewHolder();
                holder.titleAuthor = (TextView) convertView.findViewById(R.id.title_author);
                holder.description = (TextView) convertView.findViewById(R.id.description);
                holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            WallpaperInfo info = mWallpaperInfos.get(position);
            holder.thumbnail.setImageDrawable(mThumbnails.get(position));
            // author not currently used
            holder.titleAuthor.setText(info.loadLabel(mPackageManager));
            try {
                holder.description.setVisibility(View.VISIBLE);
                holder.description.setText(Html.fromHtml(
                        info.loadDescription(mPackageManager).toString()));
            } catch (Resources.NotFoundException e) {
                holder.description.setVisibility(View.GONE);
            }

            return convertView;
        }
    }
}
