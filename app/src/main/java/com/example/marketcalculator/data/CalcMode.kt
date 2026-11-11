package com.example.marketcalculator.data

enum class CalcMode {
    /** Enter the listed price, see the net income. */
    HARGA_JUAL,

    /** Enter the price the buyer pays after discount, see the listed price. */
    TARGET_HARGA,

    /** Enter the desired net income, see listed price, buyer price, and discount. */
    TARGET_BERSIH
}
