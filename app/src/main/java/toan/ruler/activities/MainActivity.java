package toan.ruler.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import butterknife.Bind;
import butterknife.ButterKnife;
import toan.ruler.R;
import toan.ruler.widget.RulerView;

public class MainActivity extends Activity {

    @Bind(R.id.spinner_unit)
    Spinner mSpinner;

    @Bind(R.id.ruler)
    RulerView mRulerView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        mRulerView.setRulerType(RulerView.RulerType.INCH);
                        break;
                    case 1:
                        mRulerView.setRulerType(RulerView.RulerType.CM);
                        break;
                    case 2:
                        mRulerView.setRulerType(RulerView.RulerType.MM);
                        break;

                }
                mRulerView.postInvalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mRulerView.setRulerType(RulerView.RulerType.INCH);
            }
        });
    }
}
