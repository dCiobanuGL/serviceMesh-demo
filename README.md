Useful links:
	https://www.baeldung.com/ops/istio-service-mesh
 	https://piotrminkowski.com/2020/02/14/local-java-development-on-kubernetes/
  	https://piotrminkowski.com/2020/06/01/service-mesh-on-kubernetes-with-istio-and-spring-boot/
   	https://piotrminkowski.com/2020/06/03/circuit-breaker-and-retries-on-kubernetes-with-istio-and-spring-boot/

Tooluri folosite:

	Prima oara trebuie ca un cluster de k8s sa ruleze (eu am folosit docker desktop). Namespace-ul folosit e cel default.
	Am folosit skaffold pt deploy-ul aplicatiei in cluster (https://skaffold.dev/docs/install/)
	Pentru impachetarea aplicatiei intr-un container am folosit Jib. (pt Jib e adaugata dependinta in pom.xml)
	Istio e folosit ca Service Mesh
	Postman
	Optional - tool de vizualizare (kiali)

Flow-ul aplicatiei:

	Sunt doua aplicatii:
		callme - o aplicatie care raspunde la calluri
		caller - aplicatia care este apelata (folosind postman) si apeleaza mai departe API-ul din callme
	Folosind postman apelezi metode din caller-service (ex: localhost:8080/caller/ping) si vei primi un raspuns text conform API-ului din callme-service

Pasi:

1. Cluster de k8s ruleaza.
2. istioctl manifest apply --set profile=demo (seteaza profilul demo pt istio; profil utilizat in medii de dev/testare, nu productie).
3. kubectl label namespace default istio-injection=enabled (pt fiecare pod o sa se injecteze automat un Envoy proxy)
4. cd callme-service
   skaffold dev --port-forward (deploy la callme)
   cd caller-service
   skaffold dev --port-forward (deploy la caller)

Fisiere folosite:

	callme-service/k8s:
		deployment: creeaza Deployment + Service (tine de kubernetes)'
		deployment: creeaza doua instante de Deployment (v1 si v2) + Service (tine de kubernetes)
		istio-rules: 
			destinationRule: defineste ce subseturi exista si cum sunt ele identificate
					         defineste metode de gestionare a traficului (traficPolicy)
			VirtualService: defineste regulile de rutare (ex: 80% din trafic se duce spre versiunea v2, iar 20% se duce spre v1)
		istio-security: metode de filtrare a traficului (ex: prin versiunea v2 singura metoda permisa este POST)
		istio-mtls: impune comunicarea prin mtls (implicit e permisiva, aici o setam sa fie STRICT)
	callme-service:
		skaffold.yaml: Contine fisierele cu care sa face deply in clusterul de k8s 
	Fisierele din caller-service se supun aceleiasi logici.
	
Comenzi utile:

	istioctl manifest apply --set profile=default --set meshConfig.accessLogFile="/dev/stdout" - mareste nivelul de logging	
	kubectl get deployments
	kubectl get pods
	kubectl get destinationrule.networking.istio.io
	kubectl scale --replicas=2 deployment/callme-service-v2
	skaffold dev --port-forward
	kubectl get authorizationpolicy
	kubectl get peerauthentication
	kubectl apply -f istio-mtls.yaml -> aplica noi reguli (continute in istio-mtls.yaml)
	
