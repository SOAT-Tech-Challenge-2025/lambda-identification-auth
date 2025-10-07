package tech.buildrun.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

public interface CriarClienteLambdaInterface {

    APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context);
}
