package com.calc.kzaf.simplecalculator

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.android.synthetic.main.activity_calculator.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


@Suppress("IMPLICIT_CAST_TO_ANY")
class Calculator : AppCompatActivity() {

    private var noConnectionFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        val actionBar = supportActionBar
        actionBar?.title = "Calculator"

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        getRequest() // Load the currency values onCreate

        validateCalculatorButtons()
        validateCurrencyButtons()
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_calculator -> {
                initializeCalculatorScreen()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_currency -> {
                if (!noConnectionFlag){
                    initializeCurrencyScreen()
                    return@OnNavigationItemSelectedListener true
                }else{
                    Toast.makeText(this, "No internet connection! Check connections and try again!", Toast.LENGTH_SHORT).show()
                    getRequest() // Load the currency values onCreate
                }

            }
        }
        false
    }

    private fun initializeCalculatorScreen(){
        calculator_linear_laout.visibility = View.VISIBLE
        currency_linear_layout.visibility = View.INVISIBLE
        clearScreen()
        first_number.text = "0"
        val actionBar = supportActionBar
        clearCurrencyScreen()
        actionBar?.title = "Calculator"
    }

    private fun initializeCurrencyScreen(){
        currency_linear_layout.visibility = View.VISIBLE
        calculator_linear_laout.visibility = View.INVISIBLE
        val actionBar = supportActionBar
        actionBar?.title = "Currency Converter"


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
        swap.setOnClickListener{ swapCurrencies() }
        clear_button.setOnClickListener{ clearCurrencyScreen() }
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

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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

            // Checks if result is a whole number or not
            when {
                valueOne - Math.floor(valueOne) == 0.0 -> {
                    val formatted = String.format("%.0f", valueOne)
                    first_number.text = formatted
                }
                else -> first_number.text = valueOne.toString()
            }
            clearScreen()

        } else {
            try {
            } catch (e: Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Currency Methods
    @SuppressLint("SetTextI18n")
    private fun currencyConvert() = when {
        !amount_value.text.isNullOrEmpty() -> {

            val namesAndValuesMap = matchCurrencyNamesWithCodes(symbolsResponse, namesResponse)

            currency_result.text =
                    calculateEquivalent(namesAndValuesMap[from_spinner.selectedItem.toString()]!!,
                            namesAndValuesMap[to_spinner.selectedItem.toString()]!!,
                            amount_value.text!!).take(9)
            currency_symbol.text = to_spinner.selectedItem.toString()

            equivalent_from.text = "1 " + from_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(namesAndValuesMap[from_spinner.selectedItem.toString()]!!,
                    namesAndValuesMap[to_spinner.selectedItem.toString()]!!, 1).take(4) + " " + to_spinner.selectedItem.toString()
            equivalent_to.text = "1 " + to_spinner.selectedItem.toString() + " ≈ " + calculateEquivalent(namesAndValuesMap[to_spinner.selectedItem.toString()]!!,
                    namesAndValuesMap[from_spinner.selectedItem.toString()]!!, 1).take(4) + " "+from_spinner.selectedItem.toString()
            equivalent_to.paintFlags = equivalent_to.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            equivalent_from.paintFlags = equivalent_from.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
        }
        else -> Toast.makeText(this, "Please set an amount", Toast.LENGTH_SHORT).show()
    }

    private fun clearCurrencyScreen(){
        currency_result.text = null
        amount_value.text = null
        equivalent_from.text = null
        equivalent_to.text = null
        currency_symbol.text = null

        from_spinner.setSelection(0)
        to_spinner.setSelection(0)
    }

    private fun swapCurrencies(){
        val fromSpinnerIndex = from_spinner.selectedItemPosition

        from_spinner.setSelection(to_spinner.selectedItemPosition)
        to_spinner.setSelection(fromSpinnerIndex)

        currencyConvert()
    }

    private var symbolsResponse: JSONObject? = null
    private var namesResponse: JSONObject? = null
    private fun getRequest() {
        val urlCurrencyRates = "http://data.fixer.io/api/latest?access_key=2f75648f00880488578eabf872c617c5"
        val urlCurrencySymbols = "http://data.fixer.io/api/symbols?access_key=2f75648f00880488578eabf872c617c5"

        val jsonObjectRequestRates = JsonObjectRequest(Request.Method.GET, urlCurrencyRates, null,
                Response.Listener { response ->
                    completionHandlerForRates(response)
                    symbolsResponse = response.getJSONObject("rates")
                    noConnectionFlag = false
                },
                Response.ErrorListener { error ->
                    noConnectionFlag = true
                }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequestRates)

        val jsonObjectRequestSymbols = JsonObjectRequest(Request.Method.GET, urlCurrencySymbols, null,
                Response.Listener { response ->
                    completionHandlerForSymbols(response)
                    namesResponse = response.getJSONObject("symbols")
                    noConnectionFlag = false
                },
                Response.ErrorListener { error ->
                    noConnectionFlag = true
                }
        )
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequestSymbols)
    }

    private var symbolsCollection = HashMap<String, Any>()
    private fun completionHandlerForSymbols(response: JSONObject?){
        symbolsCollection = collectAllRates(response?.getJSONObject("symbols")!!)
    }

    private var ratesCollection = HashMap<String, Any>()
    private fun completionHandlerForRates(response: JSONObject?) {
        ratesCollection = collectAllRates(response?.getJSONObject("rates")!!)

        val currencyNames = ArrayList(ratesCollection.keys)
        currencyNames.sort()

        val currencySymbols = ArrayList(symbolsCollection.values)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencySymbols)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        from_spinner.adapter = adapter
        to_spinner.adapter = adapter
        from_spinner.setTitle("From")
        to_spinner.setTitle("To")
    }

    private val currencyCodesAndSymbols: HashMap<String, Any> = hashMapOf()
    private fun matchCurrencyNamesWithCodes(rates: JSONObject?, names: JSONObject?): HashMap<String, Any>{

        for (i in 0 until rates!!.names().length()) {
            val keyRates = rates.names().getString(i)
            val valueRates = rates.get(keyRates)
            val keyNames = names!!.names().getString(i)
            val valueNames = names.get(keyNames).toString()
            if(keyRates == keyNames){ currencyCodesAndSymbols[valueNames] = valueRates }
        }
        return currencyCodesAndSymbols
    }

    private fun collectAllRates(rates: JSONObject): HashMap<String, Any>{
        val ratesHashMap: HashMap<String, Any> = hashMapOf()

        (0 until rates.names().length()).forEach { i ->
            val key = rates.names().getString(i)
            val value = rates.get(key)

            ratesHashMap[key] = value
        }
        return ratesHashMap
    }

    private fun calculateEquivalent(currency1: Any, currency2: Any, amount: Any): String =
            (amount.toString().toDouble() * currency2.toString().toDouble() / currency1.toString().toDouble()).toString()
    }