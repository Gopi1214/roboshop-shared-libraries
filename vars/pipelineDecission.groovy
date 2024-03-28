#!groovy

def decidePipeline(Map configMap){
    application = configMap.get("application")
    switch(application) {            
         // Each case statement section has a break condition to exit the loop 	
         case 'nodejsVM': 
            nodejsVM(configMap)
            println("This is for nodejsVM")
            break
         case 'javaVM': 
            javaVM(configMap)
            println("This is for nodejsVM")
            break
         case 'nodejsEKS': 
            nodejsEKS(configMap)
            println("This is for nodejsVM")
            break
         default: 
            error ("application is not recognised")
            break
    }
}
