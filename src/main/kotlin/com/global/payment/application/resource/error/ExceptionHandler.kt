//package com.global.payment.application.resource.error
//
//import com.global.payment.domain.user.exception.MissingUserException
//import kotlinx.coroutines.reactor.mono
//import org.springframework.beans.factory.ObjectProvider
//import org.springframework.boot.autoconfigure.web.WebProperties
//import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
//import org.springframework.boot.web.reactive.error.ErrorAttributes
//import org.springframework.context.ApplicationContext
//import org.springframework.core.annotation.Order
//import org.springframework.http.HttpStatus
//import org.springframework.http.codec.ServerCodecConfigurer
//import org.springframework.stereotype.Component
//import org.springframework.web.reactive.function.server.RequestPredicates
//import org.springframework.web.reactive.function.server.RouterFunction
//import org.springframework.web.reactive.function.server.RouterFunctions
//import org.springframework.web.reactive.function.server.ServerRequest
//import org.springframework.web.reactive.function.server.ServerResponse
//import org.springframework.web.reactive.result.view.ViewResolver
//import java.util.stream.Collectors
//
//@Component
//@Order(-1)
//class ExceptionHandler(
//    errorAttributes: ErrorAttributes,
//    resources: WebProperties,
//    applicationContext: ApplicationContext,
//    viewResolvers: ObjectProvider<ViewResolver>,
//    serverCodecConfigurer: ServerCodecConfigurer
//) : AbstractErrorWebExceptionHandler(errorAttributes, resources.resources, applicationContext) {
//
//    init {
//        this.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()))
//        this.setMessageWriters(serverCodecConfigurer.writers)
//        this.setMessageReaders(serverCodecConfigurer.readers)
//    }
//
//    override fun getRoutingFunction(
//        errorAttributes: ErrorAttributes?
//    ): RouterFunction<ServerResponse> = RouterFunctions.route(RequestPredicates.all()) { request ->
//        mono {
//            handleException(getError(request), request)
//        }
//    }
//
//    private suspend fun handleException(ex: Throwable, request: ServerRequest): ServerResponse = ex.handle(request) {
//        when (ex) {
//            is MissingUserException -> HttpStatus.BAD_REQUEST to ErrorResponseDTO(ex.message)
//            else -> HttpStatus.INTERNAL_SERVER_ERROR to ErrorResponseDTO(ex.message ?: "Unexpected Error")
//        }
//    }
//}
