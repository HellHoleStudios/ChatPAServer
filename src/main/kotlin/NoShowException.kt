package top.hhs.xgn

/**
 * An exception that does not show name to the frontend when thrown.
 *
 * It is used to trigger a frontend error page manually
 */
class NoShowException:RuntimeException {
    constructor():super()
    constructor(msg:String):super(msg)
    constructor(msg:String,cause:Throwable):super(msg,cause)
    constructor(cause:Throwable):super(cause)
}