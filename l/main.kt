package l

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class AddUseCase {
    fun add(vararg args: Int): Int {
        return args.sum()
    }
}

class AddUseCaseTest {
    lateinit var addUseCase: AddUseCase

    @BeforeClass
    fun setup() {
        addUseCase = AddUseCase()
    }

    @Test
    fun `1 더하기 2는 3이다`() {
        val result = addUseCase.add(1, 2)
        assertEquals(3, result)
    }
}

class TestCoroutineschedule {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 조절 테스트`() {
        val testCoroutineScheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(testCoroutineScheduler)
        val testCoroutineScope = CoroutineScope(testDispatcher)

        //Given
        var result = 0

        //when
        testCoroutineScope.launch {
            delay(10_000)
            result = 1
            delay(10_000)
            result = 2
            println(Thread.currentThread().name)
        }

        //Then
        assertEquals(0, result)
        testCoroutineScheduler.advanceTimeBy(5_000L)
        assertEquals(0, result)
        testCoroutineScheduler.advanceTimeBy(6_000L)
        assertEquals(1, result)
        testCoroutineScheduler.advanceTimeBy(10_000L)
        assertEquals(2, result)
    }
}

class TestCoroutineschedule2 {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 조절 테스트2`() {
        val testDispatcher = StandardTestDispatcher()
        val testCoroutineScope = CoroutineScope(testDispatcher)

        //Given
        var result = 0

        //when
        testCoroutineScope.launch {
            delay(10_000)
            result = 1
            delay(10_000)
            result = 2
            println(Thread.currentThread().name)
        }
        testDispatcher.scheduler.advanceUntilIdle() // 내부 코루틴 모두 실행시킴

        //Then
        assertEquals(2, result)
    }
}

class TestCoroutineschedule3 {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 조절 테스트3`() {

        val testCoroutineScope = TestScope()

        //Given
        var result = 0

        //when
        testCoroutineScope.launch {
            delay(10_000)
            result = 1
            delay(10_000)
            result = 2
            println(Thread.currentThread().name)
        }
        testCoroutineScope.advanceUntilIdle() // 내부 코루틴 모두 실행시킴

        //Then
        assertEquals(2, result)
    }
}

class TestCoroutineschedule4 {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 조절 테스트4`() = runTest { //this : TestScope
//Given
        var result = 0
        launch {
            delay(10000)
            result = 1
        }
        advanceUntilIdle()
        println(result)
    }
}
