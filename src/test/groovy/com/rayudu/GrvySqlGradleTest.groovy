package com.rayudu

import org.junit.Test

class GrvySqlGradleTest {

    @Test
    void buildSelectSql() {
      println  dSB().select('Example') {
          where(['x = :y'])
      }.build()

    }

    private dSB() {
        new DynamicSqlBuilder()
    }

}