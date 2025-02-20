package HelperFunctions;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.weatherfocus.wf.R;

public class graphMarkerView extends MarkerView {
    private TextView textView;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */
    public graphMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        textView = (TextView) findViewById(R.id.textView);
    }

    @Override
    public void setOffset(MPPointF offset) {
        super.setOffset(offset);
    }
private MPPointF mOffset;
    @Override
    public MPPointF getOffset() {
        if(mOffset == null){
            mOffset = new MPPointF(-(getWidth()/2), -getHeight());
        }
        return super.getOffset();
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        textView.setText(""+e.getY());
        super.refreshContent(e, highlight);
    }
}
