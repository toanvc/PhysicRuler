package toan.ruler.activities

import android.app.Activity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Switch

import butterknife.BindView
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_main.*
import toan.ruler.R
import toan.ruler.widget.RulerView

class MainActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)
        switch_unit.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                ruler.setRulerType(RulerView.RulerType.CM)
            } else {
                ruler.setRulerType(RulerView.RulerType.INCH)
            }
            ruler.postInvalidate()
        }
    }
}
