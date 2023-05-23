package com.nineya.springboot.entity;

public class TestResultVO
{
    TestResult testResult;
    Mse mse;

    public TestResult getTestResult()
    {
        return testResult;
    }

    public void setTestResult(TestResult testResult)
    {
        this.testResult = testResult;
    }

    public Mse getMse()
    {
        return mse;
    }

    public void setMse(Mse mse)
    {
        this.mse = mse;
    }

    @Override
    public String toString()
    {
        return "TestResultVO{" + "testResult=" + testResult + ", mse=" + mse + '}';
    }
}
