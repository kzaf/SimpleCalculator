package com.calc.kzaf.simplecalculator

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_calculator.*

class Calculator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        validateButtons()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_calculator -> {
                add_button.text = "+"
                sub_button.text = "-"
                multiply_button.text = "*"
                div_button.text = "/"
                clearScreen()
                first_number.text = "0"
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_currency -> {
                add_button.text = "$"
                sub_button.text = "€"
                multiply_button.text = "£"
                div_button.text = "can$"
                clearScreen()
                first_number.text = "0"
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun validateButtons(){
        zero.setOnClickListener  { updateNumbers(0) }
        one.setOnClickListener   { updateNumbers(1) }
        two.setOnClickListener   { updateNumbers(2) }
        three.setOnClickListener { updateNumbers(3) }
        four.setOnClickListener  { updateNumbers(4) }
        five.setOnClickListener  { updateNumbers(5) }
        six.setOnClickListener   { updateNumbers(6) }
        seven.setOnClickListener { updateNumbers(7) }
        eight.setOnClickListener { updateNumbers(8) }
        nine.setOnClickListener  { updateNumbers(9) }

        add_button.setOnClickListener      { updateSymbols("+") }
        sub_button.setOnClickListener      { updateSymbols("-") }
        multiply_button.setOnClickListener { updateSymbols("*") }
        div_button.setOnClickListener      { updateSymbols("/") }

        result.setOnClickListener { calculate() }
        clear.setOnClickListener {
            clearScreen()
            first_number.text = "0"
        }

        comma.setOnClickListener{
            when {
                operator.visibility == View.VISIBLE -> {
                    second_num.visibility = View.VISIBLE
                    second_num.text = second_num.text.toString() + "."
                }
                else -> first_number.text = first_number.text.toString() + "."
            }
        }
    }

    private fun clearScreen(){
        second_num.text = "0"
        second_num.visibility = View.INVISIBLE
        operator.visibility = View.INVISIBLE
    }

    private fun updateSymbols(symbol : String){
        operator.text = symbol
        operator.visibility = View.VISIBLE
    }

    private fun updateNumbers(number: Int){
        when {
            operator.visibility == View.VISIBLE -> {
                second_num.visibility = View.VISIBLE
                when {
                    second_num.text.toString() == "0" -> second_num.text = number.toString()
                    else -> second_num.text = second_num.text.toString() + number.toString()
                }
            }
            else -> when {
                first_number.text.toString() == "0" -> first_number.text = number.toString()
                else -> first_number.text = first_number.text.toString() + number.toString()
            }
        }
    }

    private fun calculate() {
        if (!(first_number.text.toString().toDouble().isNaN() || second_num.text.toString().toDouble().isNaN())){

            var valueOne = first_number.text.toString().toDouble()
            val valueTwo = second_num.text.toString().toDouble()

            when {
                operator.text.toString() === "+" -> valueOne += valueTwo
                operator.text.toString() === "-" -> valueOne -= valueTwo
                operator.text.toString() === "*" -> valueOne *= valueTwo
                operator.text.toString() === "/" -> valueOne /= valueTwo
            }

            first_number.text = valueOne.toString()
            clearScreen()

        } else {
            try {
                //valueOne = java.lang.Double.parseDouble(first_number.text.toString()).toInt()
            } catch (e: Exception) {
            }

        }
    }

}
