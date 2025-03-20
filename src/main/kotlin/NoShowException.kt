package top.hhs.xgn

/**
 * An exception that should not be displayed to the user
 *
 * Usually used as intended
 */
class NoShowException:RuntimeException {
    constructor():super()
    constructor(msg:String):super(msg)
    constructor(msg:String,cause:Throwable):super(msg,cause)
    constructor(cause:Throwable):super(cause)
}