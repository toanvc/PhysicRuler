package toan.ruler.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import butterknife.Bind;
import butterknife.ButterKnife;
import toan.ruler.R;
import toan.ruler.widget.RulerView;

public class MainActivity extends Activity {

    @Bind(R.id.switch_unit)
    Switch mSwitch;

    @Bind(R.id.ruler)
    RulerView mRulerView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mRulerView.setRulerType(RulerView.RulerType.CM);
                } else {
                    mRulerView.setRulerType(RulerView.RulerType.INCH);
                }
                mRulerView.postInvalidate();
            }
        });
    }
}
