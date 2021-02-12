# vald-seed-operator

[![Docker Pulls](https://img.shields.io/docker/pulls/rinx/vald-seed-operator.svg?style=flat-square)](https://hub.docker.com/r/rinx/vald-seed-operator)

This is a K8s operator that insert/delete declared datasets into [Vald](https://github.com/vdaas/vald).

This project is building based on [java-operator-sdk example](https://github.com/java-operator-sdk/java-operator-sdk) and [vald-client-clj](https://github.com/vdaas/vald-client-clj).

Usage
---

Before install this operator, please deploy Vald on your K8s cluster.

### Apply CRD

First, apply CRD to your cluster.

    $ kubectl apply -f crd/crd.yaml

Then you can create Seeder custom resources like crd/seeder.yaml.


### Insert data

Next, deploy vald-seed-operator by applying k8s/deployment.yaml.

    $ kubectl apply -f k8s/deployment.yaml

After vald-seed-operator pod becomes ready, let's apply Seeder resource.
Please check the correct `host`, `port` are set in the crd/seeder.yaml before applying.

    $ kubectl apply -f crd/seeder.yaml

After applying Seeder rerource, vald-seed-operator will insert the data declared in `edn` field.
The inserted IDs are listed in the status field in the Seeder resource.

    $ kubectl describe seeder/seed-test


### Delete data

Once the Seeder resource is deleted, vald-seed-operator will delete the inserted data by the resource automatically.

    $ kubectl delete crd/seeder.yaml
