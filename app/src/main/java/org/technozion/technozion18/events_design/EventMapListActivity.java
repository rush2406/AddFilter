package org.technozion.technozion18.events_design;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;


import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;

import org.technozion.technozion18.R;
import org.technozion.technozion18.common.OnEntitiesReceivedListener;
import org.technozion.technozion18.events_design.utils.DecodeBitmapTask;
import org.technozion.technozion18.models.Event;
import org.technozion.technozion18.presenters.EventPresenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class EventMapListActivity extends AppCompatActivity {
    private CardSliderLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private TextSwitcher temperatureSwitcher;
    private ImageSwitcher mapSwitcher;
    private TextSwitcher placeSwitcher;
    private TextSwitcher clockSwitcher;
    private TextSwitcher descriptionsSwitcher;
    private final int[] pics = {R.drawable.p1, R.drawable.p2, R.drawable.p3, R.drawable.p4, R.drawable.p5};
    private final String[] countries = {"Nilesh", "Hawaai", "Beach", "BEIJING", "THIRA"};
    private final String[] places = {"Sinha", "America", "Paani", "Temple of Heaven", "Aegeana Sea"};
    private final String[] temperatures = {"21°C", "19°C", "17°C", "23°C", "20°C"};
    private final String[] times = {"Aug 1 - Dec 15    7:00-18:00", "Sep 5 - Nov 10    8:00-16:00", "Mar 8 - May 21    7:00-18:00"};
    private final int[] descriptions = {R.string.text1, R.string.text2, R.string.text3, R.string.text4, R.string.text5};
    private final int[] maps = {R.drawable.map_paris, R.drawable.map_seoul, R.drawable.map_london, R.drawable.map_beijing, R.drawable.map_greece};


    private final SliderAdapter sliderAdapter = new SliderAdapter(pics, 20, new OnCardClickListener());


    private DecodeBitmapTask decodeMapBitmapTask;
    private DecodeBitmapTask.Listener mapLoadListener;

    private final int[][] dotCoords = new int[5][2];
    private View greenDot;

    private TextView country1TextView;
    private TextView country2TextView;

    private int countryOffset1;
    private int countryOffset2;
    private long countryAnimDuration;
    private int currentPosition;
    
   ArrayList<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_map_list);
        
        initRecyclerView();
        initCountryText();
        initSwitchers();
        initGreenDot();

        new EventPresenter().getEvents(new OnEntitiesReceivedListener<Event>(this) {
            @Override
            public void onReceived(List<Event> entities) {
                events.addAll(entities);
                sliderAdapter.notifyDataSetChanged();
            }
        });
        
    }
    private void initRecyclerView() {
        recyclerView= findViewById(R.id.recycler_view);
        recyclerView.setAdapter(sliderAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState== RecyclerView.SCROLL_STATE_IDLE){
                    onActiveCardChange();
                }
            }
        });
        layoutManager= (CardSliderLayoutManager) recyclerView.getLayoutManager();
        new CardSnapHelper().attachToRecyclerView(recyclerView);

    }

    private void onActiveCardChange() {
        final int pos = layoutManager.getActiveCardPosition();
        if (pos == RecyclerView.NO_POSITION || pos == currentPosition) {
            return;
        }

        onActiveCardChange(pos);
    }

    private void onActiveCardChange(int pos) {

        int animH[] = new int[] {R.anim.slide_in_right, R.anim.slide_out_left};
        int animV[] = new int[] {R.anim.slide_in_top, R.anim.slide_out_bottom};

        final boolean left2right = pos < currentPosition;
        if (left2right) {
            animH[0] = R.anim.slide_in_left;
            animH[1] = R.anim.slide_out_right;

            animV[0] = R.anim.slide_in_bottom;
            animV[1] = R.anim.slide_out_top;
        }
        setCountryText(countries[pos % countries.length], left2right);
        temperatureSwitcher.setInAnimation(EventMapListActivity.this, animH[0]);
        temperatureSwitcher.setOutAnimation(EventMapListActivity.this, animH[1]);
        temperatureSwitcher.setText(temperatures[pos % temperatures.length]);

        placeSwitcher.setInAnimation(EventMapListActivity.this, animV[0]);
        placeSwitcher.setOutAnimation(EventMapListActivity.this, animV[1]);
        placeSwitcher.setText(places[pos % places.length]);

        clockSwitcher.setInAnimation(EventMapListActivity.this, animV[0]);
        clockSwitcher.setOutAnimation(EventMapListActivity.this, animV[1]);
        clockSwitcher.setText(times[pos % times.length]);

        descriptionsSwitcher.setText(getString(descriptions[pos % descriptions.length]));

        showMap(maps[pos % maps.length]);

        ViewCompat.animate(greenDot)
                .translationX(dotCoords[pos % dotCoords.length][0])
                .translationY(dotCoords[pos % dotCoords.length][1])
                .start();

        currentPosition = pos;
    }

    private void showMap(@DrawableRes int resId) {
        if (decodeMapBitmapTask != null) {
            decodeMapBitmapTask.cancel(true);
        }

        final int w = mapSwitcher.getWidth();
        final int h = mapSwitcher.getHeight();

        decodeMapBitmapTask = new DecodeBitmapTask(getResources(), resId, w, h, mapLoadListener);
        decodeMapBitmapTask.execute();
    }


    private void setCountryText(String text, boolean left2right) {
        final TextView invisibleText;
        final TextView visibleText;
        if (country1TextView.getAlpha() > country2TextView.getAlpha()) {
            visibleText = country1TextView;
            invisibleText = country2TextView;
        } else {
            visibleText = country2TextView;
            invisibleText = country1TextView;
        }

        final int vOffset;
        if (left2right) {
            invisibleText.setX(0);
            vOffset = countryOffset2;
        } else {
            invisibleText.setX(countryOffset2);
            vOffset = 0;
        }

        invisibleText.setText(text);

        final ObjectAnimator iAlpha = ObjectAnimator.ofFloat(invisibleText, "alpha", 1f);
        final ObjectAnimator vAlpha = ObjectAnimator.ofFloat(visibleText, "alpha", 0f);
        final ObjectAnimator iX = ObjectAnimator.ofFloat(invisibleText, "x", countryOffset1);
        final ObjectAnimator vX = ObjectAnimator.ofFloat(visibleText, "x", vOffset);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(iAlpha, vAlpha, iX, vX);
        animSet.setDuration(countryAnimDuration);
        animSet.start();


    }


    private void initSwitchers() {
        temperatureSwitcher = (TextSwitcher) findViewById(R.id.ts_temperature);
        temperatureSwitcher.setFactory(new TextViewFactory(R.style.Base_CardView, true));
        temperatureSwitcher.setCurrentText(temperatures[0]);

        placeSwitcher = (TextSwitcher) findViewById(R.id.ts_place);
        placeSwitcher.setFactory(new TextViewFactory(R.style.Base_CardView, false));
        placeSwitcher.setCurrentText(places[0]);

        clockSwitcher = (TextSwitcher) findViewById(R.id.ts_clock);
        clockSwitcher.setFactory(new TextViewFactory(R.style.Base_CardView, false));
        clockSwitcher.setCurrentText(times[0]);

        descriptionsSwitcher = (TextSwitcher) findViewById(R.id.ts_description);
        descriptionsSwitcher.setInAnimation(this, android.R.anim.fade_in);
        descriptionsSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        descriptionsSwitcher.setFactory(new TextViewFactory(R.style.Base_CardView, false));
        descriptionsSwitcher.setCurrentText(getText(descriptions[0]));

        mapSwitcher = (ImageSwitcher) findViewById(R.id.ts_map);
        mapSwitcher.setInAnimation(this, R.anim.fade_in);
        mapSwitcher.setOutAnimation(this, R.anim.fade_out);
        mapSwitcher.setFactory(new ImageViewFactory());
        mapSwitcher.setImageResource(maps[0]);

    }
    @SuppressLint("ResourceType")
    private void initCountryText() {
        countryAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        countryOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        countryOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        country1TextView = (TextView) findViewById(R.id.tv_country_1);
        country2TextView = (TextView) findViewById(R.id.tv_country_2);

        country1TextView.setX(countryOffset1);
        country2TextView.setX(countryOffset2);
        country1TextView.setText(countries[0]);
        country2TextView.setAlpha(0f);

//        country1TextView.setTypeface(Typeface.createFromAsset(getAssets(),"C:\\Users\\Nilesh\\Downloads\\cardslider-android-master\\cardslider-android-master\\Sample2\\app\\src\\main\\java\\assets\\open-sans-extrabold.ttf"));
    //    country2TextView.setTypeface(Typeface.createFromAsset(getAssets(),"C:\\Users\\Nilesh\\Downloads\\cardslider-android-master\\cardslider-android-master\\Sample2\\app\\src\\main\\java\\assets\\open-sans-extrabold.ttf"));
    }
    private void initGreenDot() {
        mapSwitcher.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapSwitcher.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                final int viewLeft = mapSwitcher.getLeft();
                final int viewTop = mapSwitcher.getTop() + mapSwitcher.getHeight() / 3;

                final int border = 100;
                final int xRange = Math.max(1, mapSwitcher.getWidth() - border * 2);
                final int yRange = Math.max(1, (mapSwitcher.getHeight() / 3) * 2 - border * 2);

                final Random rnd = new Random();

                for (int i = 0, cnt = dotCoords.length; i < cnt; i++) {
                    dotCoords[i][0] = viewLeft + border + rnd.nextInt(xRange);
                    dotCoords[i][1] = viewTop + border + rnd.nextInt(yRange);
                }

                greenDot = findViewById(R.id.green_dot);
                greenDot.setX(dotCoords[0][0]);
                greenDot.setY(dotCoords[0][1]);
            }
        });
    }


    private class TextViewFactory implements  ViewSwitcher.ViewFactory {

        @StyleRes
        final int styleId;
        final boolean center;

        TextViewFactory(@StyleRes int styleId, boolean center) {
            this.styleId = styleId;
            this.center = center;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View makeView() {
            final TextView textView = new TextView(EventMapListActivity.this);

            if (center) {
                textView.setGravity(Gravity.CENTER);
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                textView.setTextAppearance(EventMapListActivity.this, styleId);
            } else {
                textView.setTextAppearance(styleId);
            }

            return textView;
        }

    }
    private class ImageViewFactory implements ViewSwitcher.ViewFactory {
        @Override
        public View makeView() {
            final ImageView imageView = new ImageView(EventMapListActivity.this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            final ViewGroup.LayoutParams lp = new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(lp);

            return imageView;
        }
    }
    private class OnCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            final CardSliderLayoutManager lm =  (CardSliderLayoutManager) recyclerView.getLayoutManager();

            if (lm.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = lm.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }

            final int clickedPosition = recyclerView.getChildAdapterPosition(view);
            if (clickedPosition == activeCardPosition) {
                final Intent intent = new Intent(EventMapListActivity.this, DetailsActivity.class);
                intent.putExtra(DetailsActivity.BUNDLE_IMAGE_ID, pics[activeCardPosition % pics.length]);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent);
                } else {
                    final CardView cardView = (CardView) view;
                    final View sharedView = cardView.getChildAt(cardView.getChildCount() - 1);
                    final ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation(EventMapListActivity.this, sharedView, "shared");
                    startActivity(intent, options.toBundle());
                }
            } else if (clickedPosition > activeCardPosition) {
                recyclerView.smoothScrollToPosition(clickedPosition);
                onActiveCardChange(clickedPosition);
            }
        }
    }
}
