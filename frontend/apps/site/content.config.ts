import { defineCollection, z } from '@nuxt/content'

const variantEnum = z.enum(['solid', 'outline', 'subtle', 'soft', 'ghost', 'link'])
const colorEnum = z.enum(['primary', 'secondary', 'neutral', 'error', 'warning', 'success', 'info'])
const sizeEnum = z.enum(['xs', 'sm', 'md', 'lg', 'xl'])

const createLinkSchema = () => z.object({
  label: z.string(),
  to: z.string(),
  icon: z.string().optional(),
  size: sizeEnum.optional(),
  trailing: z.boolean().optional(),
  target: z.enum(['_blank', '_self']).optional(),
  color: colorEnum.optional(),
  variant: variantEnum.optional()
})

export const collections = {
  home: defineCollection({
    source: 'home.yml',
    type: 'data',
    schema: z.object({
      seo: z.object({
        title: z.string(),
        description: z.string()
      }).optional(),
      title: z.string(),
      description: z.string(),
      hero: z.object({
        links: z.array(z.object({
          label: z.string(),
          to: z.string(),
          icon: z.string().optional(),
          trailing: z.boolean().optional(),
          color: z.enum(['primary', 'secondary', 'neutral', 'error', 'warning', 'success', 'info']).optional(),
          variant: z.enum(['solid', 'outline', 'subtle', 'soft', 'ghost', 'link']).optional(),
          size: z.enum(['xs', 'sm', 'md', 'lg', 'xl']).optional(),
          target: z.enum(['_blank', '_self']).optional()
        }))
      }),
      features: z.object({
        title: z.string(),
        description: z.string(),
        items: z.array(z.object({
          title: z.string(),
          description: z.string(),
          icon: z.string()
        }))
      }),
      steps: z.object({
        title: z.string(),
        description: z.string(),
        items: z.array(z.object({
          title: z.string(),
          description: z.string(),
          icon: z.string().optional()
        }))
      }),
      cta: z.object({
        title: z.string(),
        description: z.string(),
        links: z.array(createLinkSchema())
      }).optional(),
      pricing: z.object({
        title: z.string(),
        description: z.string(),
        plans: z.array(z.object({
          title: z.string(),
          description: z.string(),
          price: z.string(),
          billing_period: z.string(),
          billing_cycle: z.string(),
          button: createLinkSchema(),
          features: z.array(z.string()),
          highlight: z.boolean().optional()
        }))
      }).optional()
    })
  }),
  docs: defineCollection({
    source: 'docs/**/*.md',
    type: 'page',
    schema: z.object({
      title: z.string(),
      description: z.string().optional(),
      order: z.number().optional()
    })
  }),
  download: defineCollection({
    source: 'download.yml',
    type: 'data',
    schema: z.object({
      title: z.string(),
      description: z.string(),
      items: z.array(z.object({
        platform: z.string(),
        icon: z.string(),
        version: z.string(),
        url: z.string(),
        description: z.string().optional()
      }))
    })
  }),
  about: defineCollection({
    source: 'about.yml',
    type: 'data',
    schema: z.object({
      title: z.string(),
      description: z.string(),
      mission: z.string().optional(),
      highlights: z.array(z.object({
        icon: z.string(),
        title: z.string(),
        description: z.string()
      })).optional(),
      license: z.object({
        type: z.string(),
        url: z.string()
      }).optional(),
      github: z.string().optional(),
      contact: z.object({
        title: z.string(),
        description: z.string(),
        channels: z.array(z.object({
          icon: z.string(),
          label: z.string(),
          description: z.string(),
          to: z.string().optional(),
          wechat: z.boolean().optional()
        }))
      }).optional(),
      team: z.array(z.object({
        name: z.string(),
        role: z.string(),
        avatar: z.string().optional(),
        github: z.string().optional()
      })).optional()
    })
  }),
  pricing: defineCollection({
    source: 'pricing.yml',
    type: 'data',
    schema: z.object({
      seo: z.object({
        title: z.string(),
        description: z.string()
      }).optional(),
      title: z.string(),
      description: z.string(),
      cards: z.array(z.object({
        title: z.string(),
        description: z.string(),
        icon: z.string().optional(),
        name: z.string().optional(),
        price: z.string(),
        billing_cycle: z.string(),
        button: createLinkSchema(),
        features: z.array(z.string()),
        highlight: z.boolean().optional()
      })),
      logos: z.object({
        title: z.string(),
        icons: z.array(z.string())
      }).optional(),
      faq: z.object({
        title: z.string(),
        description: z.string(),
        items: z.array(z.object({
          label: z.string(),
          content: z.string()
        }))
      }).optional()
    })
  }),
  modules: defineCollection({
    source: 'modules.yml',
    type: 'data',
    schema: z.object({
      seo: z.object({
        title: z.string(),
        description: z.string()
      }).optional(),
      title: z.string(),
      description: z.string(),
      categories: z.array(z.object({
        title: z.string(),
        description: z.string(),
        icon: z.string(),
        modules: z.array(z.object({
          name: z.string(),
          description: z.string(),
          icon: z.string().optional(),
          tags: z.array(z.string()).optional(),
          status: z.enum(['stable', 'beta', 'planned']).optional()
        }))
      })),
      cta: z.object({
        title: z.string(),
        description: z.string(),
        links: z.array(createLinkSchema())
      }).optional()
    })
  })
}
