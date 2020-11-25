package com.example.tipcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val INITIAL_TIP_PERCENT = 15
private const val INITIAL_SPLIT = 0
private const val EURO_TO_DOLLAR_RATE = 1.18 //dollar to euro (divide)
private const val YEN_TO_DOLLAR_RATE = 0.0095 //dollar to yen
private const val YUAN_TO_DOLLAR_RATE = 0.15 //dollar to yuan
private const val YEN_TO_EURO_RATE = 0.008 //euro to yen
private const val YUAN_TO_EURO_RATE = 0.13 // euro to yuan
private const val YUAN_TO_YEN_RATE = 15.78 //yen to yuan

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        seekBar.progress = INITIAL_TIP_PERCENT
        splitBar.progress = INITIAL_SPLIT
        tvTipPercent.text = "$INITIAL_TIP_PERCENT%"
        updateTipDescription(INITIAL_TIP_PERCENT)

        val adapter = ArrayAdapter.createFromResource(this, R.array.currencies, R.layout.spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter


        var previous: String = "Dollar" //default first
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adaptView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //if there is a number inputted in the edit text field, we update it to selected
                //currency and re-computeTipAndTotal()
                val currency = adaptView?.getItemAtPosition(position).toString()
                if (etBase.text.toString().trim().isNotEmpty() && currency != previous) {
                    val baseAmt = etBase.text.toString().toDouble()
                    computeNewBase(baseAmt, previous, currency)
                    etBase.setSelection(etBase.text.length)
                    computeTipAndTotal()
                }
                previous = currency //reassign previous to current selection
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        seekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                //tag is used to identify the source of a log message
                //msg is the message you would like logged
                Log.i(TAG, "onProgressChanged output: $p1")
                tvTipPercent.text = "$p1%"
                computeTipAndTotal()
                updateTipDescription(p1)
                computePerPerson()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        etBase.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.i(TAG, "afterTextChanged output: $p0")
                computeTipAndTotal()
                computePerPerson()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        })

        splitBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                tvSplitNum.text = "$progress"
                //if no base amount is entered, don't execute this
                if (etBase.text.toString().trim().isNotEmpty()) computePerPerson()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })
    }

    private fun computeNewBase(baseAmt: Double, previous: String, currency: String) {
        var newBase: Double = 0.0
        when {
            (currency == "Dollar") -> when {
                (previous == "Euro") -> newBase = baseAmt * EURO_TO_DOLLAR_RATE
                (previous == "Yen") -> newBase = baseAmt * YEN_TO_DOLLAR_RATE
                (previous == "Yuan") -> newBase = baseAmt * YUAN_TO_DOLLAR_RATE
            }
            (currency == "Euro") -> when {
                (previous == "Dollar") -> newBase = baseAmt / EURO_TO_DOLLAR_RATE
                (previous == "Yen") -> newBase = baseAmt * YEN_TO_EURO_RATE
                (previous == "Yuan") -> newBase = baseAmt * YUAN_TO_EURO_RATE
            }
            (currency == "Yen") -> when {
                (previous == "Dollar") -> newBase = baseAmt / YEN_TO_DOLLAR_RATE
                (previous == "Euro") -> newBase = baseAmt / YEN_TO_EURO_RATE
                (previous == "Yuan") -> newBase = baseAmt * YUAN_TO_YEN_RATE
            }
            (currency == "Yuan") -> when {
                (previous == "Dollar") -> newBase = baseAmt / YUAN_TO_DOLLAR_RATE
                (previous == "Euro") -> newBase = baseAmt / YUAN_TO_EURO_RATE
                (previous == "Yen") -> newBase = baseAmt / YUAN_TO_YEN_RATE
            }
        }
        etBase.setText("%.2f".format(newBase))
    }

    private fun computePerPerson() {
        if (etBase.text.toString().isEmpty()) {
            //update tip and total to be empty
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            tvPersonAmt.text = ""
            return
        }
        val totalAmt= tvTotalAmount.text.toString().toDouble()
        val splitNum = splitBar.progress
        //handles zero exception
        if (splitNum != 0) {
            val perPerson = totalAmt / splitNum
            tvPersonAmt.text = "%.2f".format(perPerson)
        } else
            tvPersonAmt.text = "%.2f".format(totalAmt)
    }


    private fun updateTipDescription(tipPercent: Int) {
        val tipDescription: String = when (tipPercent) {
            in 0..9 -> "\uD83D\uDE1E"
            in 10..14 -> "\uD83D\uDE10"
            in 15..19 -> "\uD83D\uDE42"
            in 20..24 -> "\uD83D\uDE00"
            else -> "\uD83E\uDD29"
        }
        tvReaction.text = tipDescription
    }

    private fun computeTipAndTotal() {
        //Get the value of the base and tip percent
        if (etBase.text.toString().isEmpty()) {
            return
        }
        val baseAmt = etBase.text.toString().toDouble()
        val tipPercent = seekBar.progress
        //compute Tip and output it to interface
        val finalTip = (baseAmt * tipPercent) / 100
        tvTipAmount.text = "%.2f".format(finalTip)
        //compute Total and output it to interface
        val finalTotal = finalTip + baseAmt
        tvTotalAmount.text = "%.2f".format(finalTotal)
    }
}