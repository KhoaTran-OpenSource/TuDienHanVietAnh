package sourcecode.CVEDictionary;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Created by khoa on 7/11/2015.
 */
public class CustomeListViewData extends SimpleCursorAdapter {

    private int mLayout;
    private Context mContext;
    private final LayoutInflater mInflater;
    private Cursor mCur;
    private int mSearchType;
    private String mQuery;

    public CustomeListViewData(Context context, int layout, Cursor c, String[] from, int[] to, int typeofsearch, String query) {
        super(context, layout, c, from, to);
        this.mLayout = layout;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mCur = c;
        mSearchType = typeofsearch;
        mQuery = query;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(mLayout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        TextView titlehanzi = (TextView) view.findViewById(R.id.res_word);
        TextView titlepinyin = (TextView) view.findViewById(R.id.res_pinyin);
        TextView titleviet = (TextView) view.findViewById(R.id.res_vietmean);
        TextView titleeng = (TextView) view.findViewById(R.id.res_englmean);

        if (this.mSearchType == 0) {
            String txtResult;
            String text = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseClass.COL_HANZI));
            text += "\t";
            String[] fields = text.split(mQuery);
            //Log.d(TAG, "lengh = " + fields.length);
            int i = 0;
            if (fields[i].length() == 0) {
                txtResult = "<font color='#FF0000'>" + mQuery + "</font>";
            } else {
                txtResult = fields[i];
            }
            i++;

            for (; i < fields.length - 1; i++) {
                if (fields[i].length() == 0) {
                    if (fields[i - 1].length() == 0) { //same expect character
                        txtResult += mQuery;
                    } else {
                        txtResult += "<font color='#FF0000'>" + mQuery + "</font>";
                        txtResult += mQuery;
                    }
                } else {
                    if (fields[i - 1].length() != 0) { //not expect character
                        txtResult += "<font color='#FF0000'>" + mQuery + "</font>";
                        txtResult += fields[i];
                    } else {
                        txtResult += fields[i];
                    }
                }
            }

            //final string
            if (i == (fields.length - 1)) {
                if (fields[i - 1].length() != 0) { //not expect character
                    txtResult += "<font color='#FF0000'>" + mQuery + "</font>";
                }
                String[] temp = fields[i].split("\t");
                for (int j = 0; j < temp.length; j++) {
                    if (temp[j].length() != 0) {
                        txtResult += temp[j];
                    }
                }
            }
            titlehanzi.setText(Html.fromHtml(txtResult));
        } else if (mSearchType == 1) {
            titlepinyin.setTextColor(Color.parseColor("red"));
        } else if (mSearchType == 3) {
            titleviet.setTextColor(Color.parseColor("red"));
        } else if (mSearchType == 2) {
            titleeng.setTextColor(Color.parseColor("red"));
        }
    }
}
