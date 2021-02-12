package org.vdaas.vald.rinx.seeder;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("seeder.rinx.vald.vdaas.org")
@Version("v1alpha1")
public class Seeder extends CustomResource<SeederSpec, SeederStatus>
    implements Namespaced {}
