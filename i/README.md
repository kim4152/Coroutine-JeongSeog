# 코루틴의 이해

이번 장에서는 코루틴 양보에 대해서 배운다.

루틴 = 일반 함수  
서브루틴 = 루틴 안에서 실행되는 함수

루틴에 의해 서브루틴이 호출되면 루틴을 실행하던 스레드는 서브루틴을 실행하는 데 사용돼 서브루틴이 완료될 때까지 루틴은 다른 작업을 할 수 없다.

코루틴 = 함께(co) 실행되는 루틴   
서로 간에 스레드를 양보하며 함께 실행.

스레드를 함께 실행 = 같은 스레드를 서로 양보한다.

## 코루틴의 스레드 양보

1. delay
2. join, await
3. yield

### delay

```kotlin
delay(n) // n 시간 동안 코루틴 일시 중단 -> 해당 스레드를 다른 코루틴에게 양보
```

```kotlin
Thread.sleep(n) // n 시간 동안 스레드 블로킹 -> 해당 코루틴이 스레드 계속 점유
```

### join과 await

join이나 await가 호출되면 해당 함수를 호출한 코루틴은 스레드를 양보하고, 이 함수의 대상이 된 코루틴 내부의 코드가 실행 완료될 때까지 일시 중단.

```kotlin
fun main() = runBlocking {
    val job = launch {
        println("1")
        delay(1_000)
        println("2")
    }
    println("3")
    job.join()
    println("4")
}
```

```kotlin
// 결과
3 -> 1 -> 2 -> 4
```

1. runBlocking 코루틴이 메인 스레드 점유. launch 함수를 호출해 launch 코루틴을 생성하지만,   
   runBlocking 코루틴이 계속해서 메인 스레드를 점유하기 때문에 launch 코루틴은 대기 상태.
2. runBlocking 코루틴이 `3 출력`
3. job.join() 실행 -> runBlocking이 메인 스레드 양보
4. 자유로워진 메인 스레드에 launch 코루틴이 보내진다. `1 출력` 후 delay()로 메인 스레드 양보
5. runBlocing 코루틴이 실행되지만 join()에 의해 launch가 완료될 때까지 실행x
6. launch의 delay가 끝나고 재개되며 `2 출력`
7. launch가 끝나고 runBlocking 재개. `4 출력`

---

코루틴이 재개되면 CoroutineDispatcher 객체는 코루틴을 다시 스레드에 할당한다.    
하지만 중단 전에 실행되던 스레드와 다를 수 있다.

이 때 Threadl.sleep()을 사용하면 코루틴은 대기시간 동안 스레드를 양보하지 않아 실행 스레드가 바뀌지 않는다.