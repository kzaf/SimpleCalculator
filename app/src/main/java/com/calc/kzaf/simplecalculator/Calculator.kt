package com.calc.kzaf.simplecalculator

import android.app.ProgressDialog
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_calculator.*
import org.json.JSONObject

class Calculator : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val actionBar = supportActionBar
        actionBar?.title = "Calculator"

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        validateCalculatorButtons()
        validateCurrencyButtons()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_calculator -> {
                calculator_linear_laout.visibility = View.VISIBLE
                currency_table.visibility = View.INVISIBLE
                clearScreen()
                first_number.text = "0"
                val actionBar = supportActionBar
                actionBar?.title = "Calculator"

                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_currency -> {
                currency_table.visibility = View.VISIBLE
                calculator_linear_laout.visibility = View.INVISIBLE
                val actionBar = supportActionBar
                actionBar?.title = "Currency Converter"

                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun validateCalculatorButtons() {
        zero.setOnClickListener { updateNumbers(0) }
        one.setOnClickListener { updateNumbers(1) }
        two.setOnClickListener { updateNumbers(2) }
        three.setOnClickListener { updateNumbers(3) }
        four.setOnClickListener { updateNumbers(4) }
        five.setOnClickListener { updateNumbers(5) }
        six.setOnClickListener { updateNumbers(6) }
        seven.setOnClickListener { updateNumbers(7) }
        eight.setOnClickListener { updateNumbers(8) }
        nine.setOnClickListener { updateNumbers(9) }

        add_button.setOnClickListener { updateSymbols("+") }
        sub_button.setOnClickListener { updateSymbols("-") }
        multiply_button.setOnClickListener { updateSymbols("*") }
        div_button.setOnClickListener { updateSymbols("/") }

        comma.setOnClickListener { commaUpdate() }

        result.setOnClickListener { calculate() }

        clear.setOnClickListener {
            clearScreen()
            first_number.text = "0"
        }
    }

    private fun validateCurrencyButtons() {
        convert_button.setOnClickListener { currencyConvert() }
    }

    // Calculator Methods
    private fun clearScreen() {
        second_num.text = "0"
        second_num.visibility = View.INVISIBLE
        operator.visibility = View.INVISIBLE
    }

    private fun updateSymbols(symbol: String) {
        operator.text = symbol
        operator.visibility = View.VISIBLE
    }

    private fun commaUpdate() {
        when {
            operator.visibility == View.VISIBLE -> when {
                !second_num.text.contains(".") -> {
                    second_num.visibility = View.VISIBLE
                    second_num.text = second_num.text.toString() + "."
                }
            }
            !first_number.text.contains(".") -> first_number.text = first_number.text.toString() + "."
        }
    }

    private fun updateNumbers(number: Int) {
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
        if (!(first_number.text.toString().toDouble().isNaN() || second_num.text.toString().toDouble().isNaN())) {

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
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }

        }
    }

    // Currency Methods
    private fun currencyConvert() {
        getRequest()
    }

    private fun getRequest() {
        val url = "http://data.fixer.io/api/latest?access_key=2f75648f00880488578eabf872c617c5"

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    //equivalent.text = "Response: %s".format(response.toString())
                    completionHandler(response)
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, error?.message, Toast.LENGTH_SHORT).show()
                    completionHandler(null)
                }
        )
        // Access the RequestQueue through singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun completionHandler(response: JSONObject?) {
        var base = response?.getString("base")
        val rates = response?.getJSONObject("rates")

        val ratesEUR = rates?.getString("EUR")
        val ratesUSD = rates?.getString("USD")
        val from = calculateEquivalent(ratesEUR!!.toDouble(), ratesUSD!!.toDouble(), 1.0)
        val to = calculateEquivalent(ratesUSD!!.toDouble(), ratesEUR!!.toDouble(), 1.0)

        equivalent_from.text = "1€ = " + to.take(4) + "$"
        equivalent_to.text = "1$ = " + from.take(4) + "€"
    }

    private fun calculateEquivalent(currency1: Double, currency2: Double, amount: Double): String{

        val result = (amount * currency1) / currency2

        return result.toString()
    }
}